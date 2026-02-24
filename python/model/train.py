import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, random_split
import torch.onnx
import time

from dataset import GuitarDataset
from network import TabPredictor

DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"
BATCH_SIZE = 64
LEARNING_RATE = 0.001
EPOCHS = 20
DATA_PATH = "data"
SAVE_PATH = "params/guitar_model.pth"
ONNX_PATH = "params/guitar_model.onnx"


def evaluate(model, loader, criterion):
    model.eval()
    total_loss = 0
    correct_notes = 0
    total_notes = 0

    with torch.no_grad():
        for batch in loader:
            small = batch["small"].to(DEVICE)
            big = batch["big"].to(DEVICE)
            labels = batch["tabs"].to(DEVICE)

            outputs = model(small, big)

            for string_idx in range(6):
                pred = outputs[string_idx]
                target = labels[:, string_idx]

                total_loss += criterion(pred, target).item()

                guesses = pred.argmax(dim=1)
                correct_notes += (guesses == target).sum().item()
                total_notes += target.size(0)

    avg_loss = total_loss / len(loader)
    avg_acc = (correct_notes / total_notes) * 100
    model.train()
    return avg_loss, avg_acc


def train():
    print(f"--- STARTING TRAINING ON {DEVICE} ---")

    full_dataset = GuitarDataset(DATA_PATH)
    total_songs = len(full_dataset.songs)
    split_song_idx = int(total_songs * 0.95)

    split_frame_idx = full_dataset.cum_length[split_song_idx]
    print(f"Splitting at Song #{split_song_idx} (Frame {split_frame_idx})")
    train_data = torch.utils.data.Subset(full_dataset, range(0, split_frame_idx))
    test_data = torch.utils.data.Subset(full_dataset, range(split_frame_idx, len(full_dataset)))

    print(f"Training on {len(train_data)} frames")
    print(f"Testing on  {len(test_data)} frames")

    train_loader = DataLoader(train_data, batch_size=BATCH_SIZE, shuffle=True, num_workers=0)
    test_loader = DataLoader(test_data, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)

    model = TabPredictor().to(DEVICE)
    optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)

    class_weights = torch.ones(26).to(DEVICE)
    class_weights[1:25] = 50.0
    criterion = nn.CrossEntropyLoss(weight=class_weights)

    model.train()
    best_acc = 0.0

    for epoch in range(EPOCHS):
        running_loss = 0.0
        start_time = time.time()

        print(f"\n--- Epoch {epoch + 1}/{EPOCHS} ---")

        for i, batch in enumerate(train_loader):
            small = batch["small"].to(DEVICE)
            big = batch["big"].to(DEVICE)
            labels = batch["tabs"].to(DEVICE)

            optimizer.zero_grad()
            outputs = model(small, big)

            batch_loss = 0
            for string_idx in range(6):
                pred = outputs[string_idx]
                target = labels[:, string_idx]
                batch_loss += criterion(pred, target)

            batch_loss.backward()
            optimizer.step()

            running_loss += batch_loss.item()

            if i % 50 == 0 and i > 0:
                print(f"   Batch {i} | Train loss: {batch_loss.item():.4f}")

        val_loss, val_acc = evaluate(model, test_loader, criterion)

        duration = (time.time() - start_time) / 60
        print(f"Epoch done after ({duration:.1f} min)")
        print(f"   Train loss: {running_loss / len(train_loader):.4f}")
        print(f"   Test loss:  {val_loss:.4f}")
        print(f"   Test acc:   {val_acc:.2f}%")

        if val_acc > best_acc:
            best_acc = val_acc
            print("new best")
            torch.save(model.state_dict(), SAVE_PATH)

            print("\n--- Exporting to ONNX ---")
            model.eval()
            dummy_small = torch.randn(1, 1, 512, 11).to(DEVICE)
            dummy_big = torch.randn(1, 1, 4096, 11).to(DEVICE)

            input_names = ["small", "big"]
            output_names = ["string_E", "string_A", "string_D", "string_G", "string_B", "string_e"]

            torch.onnx.export(
                model,
                (dummy_small, dummy_big),
                ONNX_PATH,
                verbose=False,
                input_names=input_names,
                output_names=output_names,
                dynamic_axes={
                    "small": {0: "batch_size"},
                    "big": {0: "batch_size"},
                    "string_E": {0: "batch_size"},
                    "string_A": {0: "batch_size"},
                    "string_D": {0: "batch_size"},
                    "string_G": {0: "batch_size"},
                    "string_B": {0: "batch_size"},
                    "string_e": {0: "batch_size"}
                }
            )
            model.train()
    print(f"Saved acc: {best_acc}")


if __name__ == "__main__":
    train()