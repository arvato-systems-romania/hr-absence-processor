import React from "react";
import "./ActionButtons.css";

const ActionButtons = ({
  onGenerate,
  onClear,
  canGenerate = false,
  isProcessing = false,
}) => {
  return (
    <div className="action-buttons">
      <button
        className={`btn btn-primary ${!canGenerate ? "btn-hidden" : ""}`}
        onClick={() => onGenerate("excel")}
        disabled={!canGenerate || isProcessing}
      >
        {isProcessing ? (
          <>
            <span className="spinner"></span>
            Processing...
          </>
        ) : (
          <>
            <span className="icon">📄</span>
            Download XLSX
          </>
        )}
      </button>

      <button
        className={`btn btn-secondary ${!canGenerate ? "btn-hidden" : ""}`}
        onClick={() => onGenerate("csv")}
        disabled={!canGenerate || isProcessing}
      >
        <span className="icon">📄</span>
        Download CSV
      </button>
    </div>
  );
};

export default ActionButtons;
