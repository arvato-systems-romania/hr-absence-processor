import React, { useState } from "react";
import FileDropZone from "./components/FileDropZone";
import ActionButtons from "./components/ActionButtons";
import EmployeeUpdateZone from "./components/EmployeeUpdateZone";
import "./App.css";

function App() {
  const [absenceFile, setAbsenceFile] = useState(null);
  const [currentEmployeesFile, setCurrentEmployeesFile] = useState(null);
  const [newEmployeesFile, setNewEmployeesFile] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState(null);

  React.useEffect(() => {
    fetchEmployeesStatus();
  }, []);
  const fetchEmployeesStatus = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/hr-processor/employees-status"
      );
      const status = await response.json();

      if (status.exists) {
        setCurrentEmployeesFile({
          name: status.name,
          size: status.size,
          lastModified: status.lastModified,
          isPersistent: true,
        });
      } else {
        setError(
          "No persistent employees file found. Please upload one first."
        );
      }
    } catch (error) {
      console.error("Error fetching employees status:", error);
      setError("Cannot connect to backend server.");
    }
  };
  const handleAbsenceFileChange = (files) => {
    console.log("Absence file changed:", files);
    setAbsenceFile(files.length > 0 ? files[0] : null);
    setError(null);
  };

  const handleNewEmployeesFile = (file) => {
    setNewEmployeesFile(file);
  };

  const handleAcceptEmployeeChanges = async () => {
    if (!newEmployeesFile) return;

    try {
      const formData = new FormData();
      formData.append("employeesFile", newEmployeesFile);

      const response = await fetch(
        "http://localhost:8080/api/hr-processor/update-employees",
        {
          method: "POST",
          body: formData,
        }
      );

      if (response.ok) {
        await fetchEmployeesStatus();
        setNewEmployeesFile(null);
        console.log("Employee changes accepted and saved to backend");
      } else {
        throw new Error("Failed to update employees file");
      }
    } catch (error) {
      console.error("Error updating employees:", error);
      setError(`Error updating employees: ${error.message}`);
    }
  };

  const handleGenerate = async (format = "excel") => {
    if (!absenceFile || !currentEmployeesFile) {
      setError("Please ensure both files are ready before processing.");
      return;
    }

    setIsProcessing(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append("absencesFile", absenceFile);
      formData.append("format", format);

      console.log("Sending files to backend");

      const response = await fetch(
        "http://localhost:8080/api/hr-processor/process",
        {
          method: "POST",
          body: formData,
        }
      );

      if (!response.ok) {
        throw new Error(
          `Server error: ${response.status} ${response.statusText}`
        );
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;

      const timestamp = new Date()
        .toISOString()
        .slice(0, 19)
        .replace(/[:-]/g, "");
      const extension = format === "csv" ? "csv" : "xlsx";
      link.download = `HR_RO_SMARTDISPO_ABSENCE_${timestamp}.${extension}`;

      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      console.log("File downloaded successfully");
    } catch (error) {
      console.error("Error processing files:", error);
      setError(`Error: ${error.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const canGenerate = absenceFile && currentEmployeesFile && !isProcessing;

  return (
    <div className="App">
      <header className="App-header">
        <h1>HR Absence Processor</h1>
        <p>Upload absence data and manage employee information</p>
      </header>

      <main className="App-main">
        <div className="current-employees">
          <h3>Current Employee File</h3>
          {currentEmployeesFile ? (
            <div className="file-display">
              <span className="file-name">{currentEmployeesFile.name}</span>
              <span className="file-size">
                {(currentEmployeesFile.size / 1024).toFixed(1)} KB
              </span>
              <span className="persistent-badge">Persistent</span>
            </div>
          ) : (
            <div className="no-file">
              <p>No persistent employees file found</p>
            </div>
          )}
        </div>

        <div className="section">
          <h3>Upload Absence Data</h3>
          <FileDropZone
            onFilesChange={handleAbsenceFileChange}
            maxFiles={1}
            title="Drag absence Excel file here"
            hint="Only .xlsx files accepted"
          />
        </div>

        <ActionButtons
          onGenerate={handleGenerate}
          canGenerate={canGenerate}
          isProcessing={isProcessing}
        />

        <EmployeeUpdateZone
          currentFile={currentEmployeesFile}
          newFile={newEmployeesFile}
          onNewFile={handleNewEmployeesFile}
          onAcceptChanges={handleAcceptEmployeeChanges}
        />

        {error && (
          <div className="error-message">
            <p>{error}</p>
          </div>
        )}

        {!error && (
          <div className="status">
            <p>
              {!absenceFile
                ? "Upload absence file to continue"
                : isProcessing
                ? "Processing files... Please wait."
                : "Ready to process! Click Generate Output."}
            </p>
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
