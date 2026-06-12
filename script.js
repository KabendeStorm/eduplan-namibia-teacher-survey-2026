/* ============================================
   EduPlan Namibia — Teacher Needs Survey
   Collects responses and sends them to Google Sheets
   ============================================ */

// ⚠️ STEP 3: PASTE YOUR GOOGLE APPS SCRIPT WEB APP URL HERE
// (You get this after deploying Code.gs — see README, Step 2.
//  It looks like: https://script.google.com/macros/s/AKfycb..../exec )
const GOOGLE_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbx1573TqFWXEMxY4oIOeo91UWD4DgDqck4Wh_CSEBZ_bPfYnd3-47B6JRHUYoA8YRlsUg/exec";

const TOTAL_QUESTIONS = 23;

// ===== PROGRESS BAR =====
function updateProgress() {
    let answered = 0;
    for (let i = 1; i <= TOTAL_QUESTIONS; i++) {
        const container = document.getElementById("q" + i);
        if (!container) continue;
        const inputs = container.querySelectorAll("input, select, textarea");
        let done = false;
        inputs.forEach(input => {
            if (input.type === "radio" || input.type === "checkbox") {
                if (input.checked) done = true;
            } else if (input.value.trim() !== "") {
                done = true;
            }
        });
        if (done) answered++;
    }
    const percent = Math.round((answered / TOTAL_QUESTIONS) * 100);
    document.getElementById("progressFill").style.width = percent + "%";
    document.getElementById("currentQuestion").textContent = answered;
}

const form = document.getElementById("surveyForm");
form.addEventListener("change", updateProgress);
form.addEventListener("input", updateProgress);

// ===== COLLECT DATA =====
// Multi-select checkboxes are joined with " | " so they sit in one cell.
function collectFormData() {
    const data = {};
    data["timestamp"] = new Date().toLocaleString("en-GB", { timeZone: "Africa/Windhoek" });

    // Single-value fields
    form.querySelectorAll(
        'input[type="radio"]:checked, select, input[type="email"], input[type="tel"], input[type="text"], textarea'
    ).forEach(field => {
        if (field.name) data[field.name] = field.value;
    });

    // Checkbox groups
    const groups = {};
    form.querySelectorAll('input[type="checkbox"]:checked').forEach(box => {
        if (!groups[box.name]) groups[box.name] = [];
        groups[box.name].push(box.value);
    });
    for (const name in groups) data[name] = groups[name].join(" | ");

    return data;
}

// ===== SUBMIT =====
form.addEventListener("submit", function (e) {
    e.preventDefault();
    const btn = document.getElementById("submitBtn");
    btn.disabled = true;
    btn.textContent = "Submitting...";

    if (GOOGLE_SCRIPT_URL === "PASTE_YOUR_WEB_APP_URL_HERE") {
        alert("⚠️ Setup needed: add your Google Apps Script URL on line 9 of script.js.");
        btn.disabled = false;
        btn.textContent = "Submit Survey";
        return;
    }

    fetch(GOOGLE_SCRIPT_URL, {
        method: "POST",
        mode: "no-cors", // required for Google Apps Script web apps
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(collectFormData())
    })
        .then(() => showSuccess())
        .catch(err => {
            console.error("Submission error:", err);
            alert("Something went wrong. Please check your connection and try again.");
            btn.disabled = false;
            btn.textContent = "Submit Survey";
        });
});

function showSuccess() {
    form.style.display = "none";
    document.querySelector(".progress-container").style.display = "none";
    document.getElementById("successMessage").style.display = "block";
    window.scrollTo({ top: 0, behavior: "smooth" });
}

updateProgress();