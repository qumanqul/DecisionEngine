import { useState } from "react";
import axios from "axios";
import "../styles/LoanForm.css";

const PERSONAL_CODES = [
  { code: "49002010965", label: "49002010965 — Debt" },
  { code: "49002010976", label: "49002010976 — Segment 1 (modifier: 100)" },
  { code: "49002010987", label: "49002010987 — Segment 2 (modifier: 300)" },
  { code: "49002010998", label: "49002010998 — Segment 3 (modifier: 1000)" },
  { code: "custom", label: "Enter custom code manually" },
];

const LoanForm = () => {
  const [formData, setFormData] = useState({
    personalCode: "",
    loanAmount: "",
    loanPeriod: "",
  });

  const [selectedOption, setSelectedOption] = useState("");
  const [result, setResult] = useState(null);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState(null);

  const validate = () => {
    const newErrors = {};

    if (!formData.personalCode.trim()) {
      newErrors.personalCode = "Personal code is required";
    }

    const amount = parseFloat(formData.loanAmount);
    if (!formData.loanAmount) {
      newErrors.loanAmount = "Loan amount is required";
    } else if (amount < 2000 || amount > 10000) {
      newErrors.loanAmount = "Amount must be between €2000 and €10000";
    }

    const period = parseInt(formData.loanPeriod);
    if (!formData.loanPeriod) {
      newErrors.loanPeriod = "Loan period is required";
    } else if (period < 12 || period > 60) {
      newErrors.loanPeriod = "Period must be between 12 and 60 months";
    }

    return newErrors;
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
  };

  const handleDropdownChange = (e) => {
    const value = e.target.value;
    setSelectedOption(value);
    setErrors({ ...errors, personalCode: undefined });

    if (value !== "custom") {
      setFormData({ ...formData, personalCode: value });
    } else {
      setFormData({ ...formData, personalCode: "" });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setResult(null);
    setErrors({});
    setServerError(null);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/loan/decision",
        {
          personalCode: formData.personalCode,
          loanAmount: parseFloat(formData.loanAmount),
          loanPeriod: parseInt(formData.loanPeriod),
        },
      );
      setResult(response.data);
    } catch (err) {
      if (err.response?.data) {
        setErrors(err.response.data);
      } else if (err.request) {
        setServerError(
          "Unable to connect to the server. Please try again later.",
        );
      } else {
        setServerError("Something went wrong. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const isApproved = result?.decision === "POSITIVE";

  return (
    <div className="page">
      <div className="card">
        <h1 className="title">Loan Decision Engine</h1>
        <p className="subtitle">
          Fill in the details to get an instant loan decision
        </p>

        <form onSubmit={handleSubmit} className="form">
          <div className="field">
            <label className="label">Select or enter personal code</label>
            <select
              className="select"
              onChange={handleDropdownChange}
              value={selectedOption}
            >
              <option value="" disabled>
                Select personal code...
              </option>
              {PERSONAL_CODES.map((item) => (
                <option key={item.code} value={item.code}>
                  {item.label}
                </option>
              ))}
            </select>
          </div>

          {(selectedOption === "custom" || selectedOption === "") && (
            <div className="field">
              <label className="label">Personal Code</label>
              <input
                type="text"
                name="personalCode"
                value={formData.personalCode}
                onChange={handleChange}
                placeholder="Enter personal code manually"
                className={`input ${errors.personalCode ? "error" : ""}`}
              />
              {errors.personalCode && (
                <span className="error-text">{errors.personalCode}</span>
              )}
            </div>
          )}

          {selectedOption !== "custom" && selectedOption !== "" && (
            <div className="field">
              {errors.personalCode && (
                <span className="error-text">{errors.personalCode}</span>
              )}
            </div>
          )}

          <div className="field">
            <label className="label">Loan Amount (€)</label>
            <input
              type="number"
              name="loanAmount"
              value={formData.loanAmount}
              onChange={handleChange}
              placeholder="2000 – 10000"
              className={`input ${errors.loanAmount ? "error" : ""}`}
            />
            {errors.loanAmount && (
              <span className="error-text">{errors.loanAmount}</span>
            )}
          </div>

          <div className="field">
            <label className="label">Loan Period (months)</label>
            <input
              type="number"
              name="loanPeriod"
              value={formData.loanPeriod}
              onChange={handleChange}
              placeholder="12 – 60"
              className={`input ${errors.loanPeriod ? "error" : ""}`}
            />
            {errors.loanPeriod && (
              <span className="error-text">{errors.loanPeriod}</span>
            )}
          </div>

          <button type="submit" disabled={loading} className="button">
            {loading ? "Processing..." : "Get Decision"}
          </button>
        </form>

        {serverError && (
          <div className="server-error">
            <span>⚠ {serverError}</span>
          </div>
        )}

        {result && (
          <div className={`result ${isApproved ? "approved" : "rejected"}`}>
            <h3
              className={`result-title ${isApproved ? "approved" : "rejected"}`}
            >
              {isApproved ? "✓ Approved" : "✗ Rejected"}
            </h3>
            {result.amount && (
              <p className="result-text">
                Approved Amount: <strong>€{result.amount}</strong>
              </p>
            )}
            {result.period && (
              <p className="result-text">
                Period: <strong>{result.period} months</strong>
              </p>
            )}
            <p className="result-text">{result.message}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default LoanForm;
