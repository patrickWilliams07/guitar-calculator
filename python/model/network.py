import torch
import torch.nn as nn


class TabPredictor(nn.Module):
    def __init__(self) -> None:
        super(TabPredictor, self).__init__()

        self.small = nn.Sequential(
            nn.Conv2d(1, 16, 3, 1, 1),
            nn.BatchNorm2d(16),
            nn.ReLU(),
            nn.MaxPool2d((2, 1)),

            nn.Conv2d(16, 32, 3, 1, 1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.MaxPool2d((2, 2)),

            nn.Conv2d(32, 64, 3, 1, 1),
            nn.BatchNorm2d(64),
            nn.ReLU(),
            nn.MaxPool2d((2, 2)),
        )

        self.small_size = self._get_flat_size(self.small, torch.zeros(1, 1, 512, 11))

        self.big = nn.Sequential(
            nn.Conv2d(1, 16, 3, 1, 1),
            nn.BatchNorm2d(16),
            nn.ReLU(),
            nn.MaxPool2d((2, 1)),

            nn.Conv2d(16, 32, 3, 1, 1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.MaxPool2d((4, 2)),

            nn.Conv2d(32, 64, 3, 1, 1),
            nn.BatchNorm2d(64),
            nn.ReLU(),
            nn.MaxPool2d((4, 2)),
        )

        self.big_size = self._get_flat_size(self.big, torch.zeros(1, 1, 4096, 11))

        self.combined = nn.Sequential(
            nn.Linear(self.small_size + self.big_size, 1024),
            nn.ReLU(),
            nn.Dropout(0.5)
        )

        self.strings = nn.ModuleList([
            nn.Linear(1024, 26) for _ in range(6)
        ])


    def forward(self, small_vals, big_vals):
        small_conv = self.small(small_vals)
        big_conv = self.big(big_vals)

        small_conv = small_conv.view(small_conv.size(0), -1)
        big_conv = big_conv.view(big_conv.size(0), -1)

        both = torch.cat((small_conv, big_conv), dim=1)
        combined = self.combined(both)

        return [string(combined) for string in self.strings]



    @staticmethod
    def _get_flat_size(network, zeros) -> int:
        with torch.no_grad():
            a = network(zeros)
            return a.numel()

