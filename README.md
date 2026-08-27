# Guitar Audio to Sheet Music converter

### A DSP and ML engine mapping raw guitar audio input to precise guitar tab with finger placements.

- Uses concurrent, windows FFTs to achieve accuracy at the low pitches alongside handling quick playing, against the natural pitch-latency trade off.
- The custom PyTorch model merges parallel CNNs into a multi-headed output for each string, partially disambiguating clashes between notes.
- 89% test set accuracy, beating the data set source.

Needs the Java Onnyx Runtime Jar installed [Link to Runtime](https://onnxruntime.ai/docs/get-started/with-java.html)
