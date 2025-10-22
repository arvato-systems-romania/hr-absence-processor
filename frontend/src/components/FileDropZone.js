import React, { useState, useCallback } from "react";
import { useDropzone } from "react-dropzone";
import "./FileDropZone.css";

const FileDropZone = ({
  onFilesChange,
  maxFiles = 1,
  title = "Drag Excel files here or click to select",
  hint = "Only .xlsx files accepted",
}) => {
  const [files, setFiles] = useState([]);

  const onDrop = useCallback(
    (acceptedFiles, rejectedFiles) => {
      const validFiles = acceptedFiles.filter(
        (file) =>
          file.type ===
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      );

      //State update
      const newFiles =
        maxFiles === 1
          ? validFiles.slice(0, 1)
          : [...files, ...validFiles].slice(0, maxFiles);
      setFiles(newFiles);
      onFilesChange(newFiles);

      if (rejectedFiles.length > 0) {
        alert("Some files were rejected. Please upload only .xlsx files.");
      }
    },
    [files, maxFiles, onFilesChange]
  );

  //Dropzone
  const { getRootProps, getInputProps, isDragActive, isDragReject } =
    useDropzone({
      onDrop,
      accept: {
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": [
          ".xlsx",
        ],
      },
      maxFiles: maxFiles,
      multiple: maxFiles > 1,
    });

  const removeFile = (indexToRemove) => {
    const newFiles = files.filter((_, index) => index !== indexToRemove);
    setFiles(newFiles);
    onFilesChange(newFiles);
  };

  return (
    <div className="file-drop-zone">
      <div
        {...getRootProps()}
        className={`dropzone ${isDragActive ? "active" : ""} ${
          isDragReject ? "reject" : ""
        }`}
      >
        <input {...getInputProps()} />

        <div className="dropzone-content">
          <div className="icon">📁</div>

          {isDragActive ? (
            isDragReject ? (
              <p className="message error">Invalid file type!</p>
            ) : (
              <p className="message success">Drop files here!</p>
            )
          ) : (
            <div>
              <p className="message">{title}</p>
              <p className="hint">{hint}</p>
            </div>
          )}
        </div>
      </div>

      {files.length > 0 && (
        <div className="files-preview">
          <div className="preview-header">
            <h4>
              Uploaded File
              {maxFiles > 1 ? `s (${files.length}/${maxFiles})` : ""}
            </h4>
          </div>

          <div className="files-list">
            {files.map((file, index) => (
              <div key={index} className="file-item">
                <div className="file-info">
                  <span className="file-name">{file.name}</span>
                  <span className="file-size">
                    {(file.size / 1024).toFixed(1)} KB
                  </span>
                </div>
                <button
                  onClick={() => removeFile(index)}
                  className="remove-btn"
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default FileDropZone;
