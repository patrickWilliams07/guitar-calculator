import os
import glob
import numpy as np
import jams
import math

SAMPLE_RATE = 44100
HOP_SIZE = 512
FRAME_DURATION = HOP_SIZE / SAMPLE_RATE

OPEN_STRINGS = [40, 45, 50, 55, 59, 64]

def midi_to_class_id(midi_pitch, string_idx):
    pitch_int = int(round(midi_pitch))
    open_pitch = OPEN_STRINGS[string_idx]
    fret = pitch_int - open_pitch
    if fret < 0:
        return 0
    if fret > 24:
        fret = 24
    return fret + 1


def process_jams_file(jams_path, output_path):
    jam = jams.load(jams_path)
    duration_sec = jam.file_metadata.duration
    num_frames = math.ceil(duration_sec / FRAME_DURATION)

    labels = np.zeros((num_frames, 6), dtype="int8")

    anns = jam.search(namespace='note_midi')

    for idx, ann in enumerate(anns):
        for note in ann:
            start_time = note.time
            duration = note.duration
            pitch = note.value

            start_frame = int(round(start_time / FRAME_DURATION))
            end_frame = int(round((start_time + duration) / FRAME_DURATION))

            if start_frame >= num_frames: continue
            end_frame = min(end_frame, num_frames)

            class_id = midi_to_class_id(pitch, idx)

            if class_id > 0:
                labels[start_frame:end_frame, idx] = class_id

    with open(output_path, 'w') as f:
        labels.tofile(f)


def convert_dataset(input_folder, output_folder):
    os.makedirs(output_folder, exist_ok=True)
    jams_files = glob.glob(os.path.join(input_folder, "*.jams"))

    for f in jams_files:
        filename = os.path.basename(f)
        bin_name = filename.replace(".jams", "_mic.jams.bin")
        out_path = os.path.join(output_folder, bin_name)

        process_jams_file(f, out_path)


# --- RUN IT ---
if __name__ == "__main__":
    INPUT_DIR = "rawData/annotation"
    OUTPUT_DIR = "data/tabs"

    convert_dataset(INPUT_DIR, OUTPUT_DIR)