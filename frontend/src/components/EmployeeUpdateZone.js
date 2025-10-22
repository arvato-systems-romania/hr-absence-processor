import React, { useState } from "react";
import { useDropzone } from "react-dropzone";
import * as XLSX from "xlsx";
import "./EmployeeUpdateZone.css";

const EmployeeUpdateZone = ({
  currentFile,
  newFile,
  onNewFile,
  onAcceptChanges,
}) => {
  const [isDragActive, setIsDragActive] = useState(false);
  const [showDiff, setShowDiff] = useState(false);
  const [currentEmployees, setCurrentEmployees] = useState([]);
  const [newEmployees, setNewEmployees] = useState([]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState(null);

  const readExcelFile = async (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const data = new Uint8Array(e.target.result);
          const workbook = XLSX.read(data, { type: "array" });
          const sheetName = workbook.SheetNames[0];
          const worksheet = workbook.Sheets[sheetName];
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });

          const employees = [];

          for (let i = 1; i < jsonData.length; i++) {
            const row = jsonData[i];
            if (row && row.length > 0 && row[0]) {
              const employee = {
                userId: row[0] || "",
                lastName: row[1] || "",
                firstName: row[2] || "",
                email: row[3] || "",
                weeklyWorkingHours: row[4] || 0,
              };
              employees.push(employee);
            }
          }

          resolve(employees);
        } catch (error) {
          reject(error);
        }
      };
      reader.onerror = reject;
      reader.readAsArrayBuffer(file);
    });
  };
  const getCurrentEmployeesFromBackend = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/hr-processor/current-employees"
      );
      if (!response.ok) {
        throw new Error("Failed to fetch current employees file");
      }
      const blob = await response.blob();
      return await readExcelFile(blob);
    } catch (error) {
      throw new Error("Could not load current employees file from server");
    }
  };

  const onDrop = async (acceptedFiles) => {
    if (acceptedFiles.length === 0) return;

    setIsProcessing(true);
    setError(null);

    try {
      const file = acceptedFiles[0];
      onNewFile(file);

      const newEmpData = await readExcelFile(file);
      setNewEmployees(newEmpData);

      let currentEmpData;
      if (currentFile && currentFile.isPersistent) {
        currentEmpData = await getCurrentEmployeesFromBackend();
      } else if (currentFile && currentFile instanceof File) {
        currentEmpData = await readExcelFile(currentFile);
      } else {
        currentEmpData = [
          {
            userId: "test001",
            lastName: "Test1",
            firstName: "Vasile",
            email: "vasile.test1@company.com",
            weeklyWorkingHours: 40,
          },
          {
            userId: "test002",
            lastName: "Vagabond",
            firstName: "Bond",
            email: "bond.vagabond@company.com",
            weeklyWorkingHours: 40,
          },
          {
            userId: "test003",
            lastName: "Eminescu",
            firstName: "Mihai",
            email: "mihai.eminescu@company.com",
            weeklyWorkingHours: 40,
          },
        ];
      }

      setCurrentEmployees(currentEmpData);
      setShowDiff(true);
    } catch (error) {
      console.error("Error reading file:", error);
      setError(`Error reading files: ${error.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const { getRootProps, getInputProps } = useDropzone({
    onDrop,
    accept: {
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": [
        ".xlsx",
      ],
    },
    maxFiles: 1,
    multiple: false,
    onDragEnter: () => setIsDragActive(true),
    onDragLeave: () => setIsDragActive(false),
  });

  const generateDiff = () => {
    if (!currentEmployees.length || !newEmployees.length) return [];

    const diff = [];

    const newEmployeesMap = new Map();
    newEmployees.forEach((emp) => {
      newEmployeesMap.set(emp.userId, emp);
    });

    currentEmployees.forEach((current) => {
      const newEmployee = newEmployeesMap.get(current.userId);

      if (!newEmployee) {
        diff.push({ ...current, status: "deleted" });
      } else {
        const isModified =
          current.lastName !== newEmployee.lastName ||
          current.firstName !== newEmployee.firstName ||
          current.email !== newEmployee.email ||
          current.weeklyWorkingHours !== newEmployee.weeklyWorkingHours;

        if (isModified) {
          diff.push({ ...newEmployee, status: "modified", oldData: current });
        } else {
          diff.push({ ...current, status: "unchanged" });
        }
      }
    });

    newEmployees.forEach((newEmp) => {
      const exists = currentEmployees.find(
        (current) => current.userId === newEmp.userId
      );
      if (!exists) {
        diff.push({ ...newEmp, status: "added" });
      }
    });

    return diff.sort((a, b) => {
      const statusPriority = {
        deleted: 1,
        added: 2,
        modified: 3,
        unchanged: 4,
      };
      const priorityDiff = statusPriority[a.status] - statusPriority[b.status];

      if (priorityDiff === 0) {
        return a.userId.localeCompare(b.userId);
      }

      return priorityDiff;
    });
  };

  const handleAccept = () => {
    onAcceptChanges();
    setShowDiff(false);
    setCurrentEmployees([]);
    setNewEmployees([]);
  };

  const handleCancel = () => {
    onNewFile(null);
    setShowDiff(false);
    setCurrentEmployees([]);
    setNewEmployees([]);
    setError(null);
  };

  return (
    <div className="employee-update-zone">
      <h3>Update Employee Data</h3>

      {error && (
        <div className="error-message">
          <p>{error}</p>
        </div>
      )}

      {!newFile ? (
        <div
          {...getRootProps()}
          className={`update-dropzone ${isDragActive ? "active" : ""}`}
        >
          <input {...getInputProps()} />
          <div className="dropzone-content">
            <div className="icon">👥</div>
            <p className="message">
              Drag new employee file here to compare changes
            </p>
            <p className="hint">Upload updated HR_RO_SMARTDISPO_WS.xlsx</p>
          </div>
        </div>
      ) : (
        <div className="update-section">
          <div className="new-file-info">
            <h4>New File: {newFile.name}</h4>
            <span className="file-size">
              {(newFile.size / 1024).toFixed(1)} KB
            </span>
          </div>

          {isProcessing && (
            <div className="processing">
              <p>Reading and comparing files...</p>
            </div>
          )}

          {showDiff && !isProcessing && (
            <div className="diff-preview">
              <h4>Changes Preview ({generateDiff().length} employees)</h4>
              <div className="diff-legend">
                <span className="legend-item added">Green = Added</span>
                <span className="legend-item modified">Blue = Modified</span>
                <span className="legend-item deleted">Red = Deleted</span>
                <span className="legend-item unchanged">Gray = Unchanged</span>
              </div>

              <div className="diff-list">
                {generateDiff().map((employee, index) => (
                  <div key={index} className={`diff-item ${employee.status}`}>
                    <div className="employee-info">
                      <strong>{employee.userId}</strong> - {employee.firstName}{" "}
                      {employee.lastName}
                      <br />
                      <span className="email">{employee.email}</span>
                      <span className="hours">
                        ({employee.weeklyWorkingHours}h/week)
                      </span>
                      {employee.status === "modified" && employee.oldData && (
                        <div className="old-data">
                          <small>
                            Was: {employee.oldData.firstName}{" "}
                            {employee.oldData.lastName} -{" "}
                            {employee.oldData.email} (
                            {employee.oldData.weeklyWorkingHours}h/week)
                          </small>
                        </div>
                      )}
                    </div>
                    <span className="status-badge">{employee.status}</span>
                  </div>
                ))}
              </div>

              <div className="diff-actions">
                <button className="btn btn-success" onClick={handleAccept}>
                  Accept Changes
                </button>
                <button className="btn btn-secondary" onClick={handleCancel}>
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default EmployeeUpdateZone;
