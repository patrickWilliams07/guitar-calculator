import torch
from torch.utils.data import Dataset
import numpy as np
import os
import glob


class GuitarDataset(Dataset):
    def __init__(self, root, context_width=11):
        self.context_width = context_width
        self.half_width = context_width // 2

        search_path = root + "/tabs/*.jams.bin"
        tab_files = sorted(glob.glob(search_path))

        self.songs = []
        self.cum_length = [0]
        length = 0

        for tab_path in tab_files:
            small_path = tab_path.replace("/tabs/", "/small/").replace(".jams.bin", ".wav.bin")
            big_path = tab_path.replace("/tabs/", "/big/").replace(".jams.bin", ".wav.bin")

            file_size = os.path.getsize(tab_path)
            sample_size = (file_size - 4) // 24

            self.songs.append({
                "tabs": tab_path,
                "small": small_path,
                "big": big_path,
                "size": sample_size
            })

            length += sample_size
            self.cum_length.append(length)

        self.length = length

    def __len__(self):
        return self.length

    def _bin_search(self, idx):
        low = 0
        high = len(self.songs) - 1
        while low < high:
            mid = (low + high) // 2
            if idx < self.cum_length[mid + 1]:
                high = mid
            else:
                low = mid + 1
        return low


    def __getitem__(self, idx):
        song_idx = self._bin_search(idx)

        song_info = self.songs[song_idx]
        centre_idx = idx - self.cum_length[song_idx]

        # 4. READ DATA
        start = centre_idx - self.half_width
        end = centre_idx + self.half_width + 1

        pad_left = 0
        pad_right = 0
        read_start = start
        read_end = end

        if read_start < 0:
            pad_left = -read_start
            read_start = 0

        if read_end > song_info["size"]:
            pad_right = read_end - song_info["size"]
            read_end = song_info["size"]

        small_mmap = np.memmap(song_info["small"], dtype='>f4', mode='r', offset=4).reshape(-1, 512)
        small_data = small_mmap[read_start:read_end].copy().astype("float32")

        big_mmap = np.memmap(song_info["big"], dtype='>f4', mode='r', offset=4).reshape(-1, 4096)
        big_data = big_mmap[read_start:read_end].copy().astype("float32")

        label_mmap = np.memmap(song_info["tabs"], dtype='int8', mode='r', offset=0).reshape(-1, 6)
        label_vec = label_mmap[centre_idx].copy().astype('int64')

        if pad_left > 0 or pad_right > 0:
            small_data = np.pad(small_data, ((pad_left, pad_right), (0, 0)), mode='constant')
            big_data = np.pad(big_data, ((pad_left, pad_right), (0, 0)), mode='constant')

        return {
            "small": torch.tensor(small_data.T).float().unsqueeze(0),
            "big": torch.tensor(big_data.T).float().unsqueeze(0),
            "tabs": torch.tensor(label_vec)
        }