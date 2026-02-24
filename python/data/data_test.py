import numpy as np

def load_fft_file(filepath):
    with open(filepath) as f:
        width_bytes = f.read(4)
        width = 1 << np.frombuffer(width_bytes, dtype='>i4')[0]

    raw_data = np.fromfile(filepath, dtype='>f4', offset=4)
    final_array = raw_data.reshape(-1, width)
    return final_array

def load_tabs_file(filepath):
    raw_data = np.fromfile(filepath, dtype='int8')
    final_array = raw_data.reshape(-1, 6)
    return final_array

fft = load_fft_file("../../data/small/00_BN2-131-B_solo_mic.wav.bin")
tabs = load_tabs_file("../../data/tabs/00_BN2-131-B_solo_mic.jams.bin")
print(fft.shape, tabs.shape)