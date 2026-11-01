// FoodFlow - Secure Authentication Client

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

let cachedCsrfToken = null;

async function fetchCsrfToken() {
  const fromCookie = getCookie("XSRF-TOKEN");
  if (fromCookie) {
    cachedCsrfToken = decodeURIComponent(fromCookie);
    return cachedCsrfToken;
  }
  try {
    const res = await fetch("/api/auth/csrf");
    if (res.ok) {
      const data = await res.json();
      cachedCsrfToken = data.token;
      return cachedCsrfToken;
    }
  } catch (err) {
    console.warn("Could not retrieve CSRF token:", err);
  }
  return "";
}

function showAlert(message, type = "error") {
  const alertEl = document.getElementById("auth-alert");
  if (!alertEl) return;
  alertEl.textContent = message;
  alertEl.className = `auth-alert ${type} show`;
}

function clearAlert() {
  const alertEl = document.getElementById("auth-alert");
  if (!alertEl) return;
  alertEl.className = "auth-alert";
  alertEl.textContent = "";
}

function setLoading(btn, isLoading, defaultText = "Submit") {
  if (!btn) return;
  btn.disabled = isLoading;
  if (isLoading) {
    btn.dataset.origText = btn.innerHTML;
    btn.innerHTML = `<span class="spinner"></span> Processing...`;
  } else {
    btn.innerHTML = btn.dataset.origText || defaultText;
  }
}

// Password toggle helper
document.querySelectorAll(".pw-toggle").forEach(btn => {
  btn.addEventListener("click", () => {
    const targetId = btn.getAttribute("data-target");
    const input = document.getElementById(targetId);
    if (!input) return;
    const isPw = input.type === "password";
    input.type = isPw ? "text" : "password";
    btn.textContent = isPw ? "🙈" : "👁";
    btn.setAttribute("aria-label", isPw ? "Hide password" : "Show password");
  });
});

// Setup Login Form
const loginForm = document.getElementById("login-form");
if (loginForm) {
  // Check if redirected with registered=true
  const params = new URLSearchParams(window.location.search);
  if (params.get("registered") === "true") {
    showAlert("Account created successfully! Please sign in with your credentials.", "success");
    const emailField = document.getElementById("login-email");
    if (emailField && params.get("email")) {
      emailField.value = params.get("email");
    }
  }

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearAlert();
    const btn = document.getElementById("login-submit-btn");
    const email = document.getElementById("login-email").value.trim();
    const password = document.getElementById("login-password").value;

    if (!email || !password) {
      showAlert("Please enter both email and password.");
      return;
    }

    setLoading(btn, true, "Sign in to FoodFlow →");

    try {
      const csrfToken = await fetchCsrfToken();
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfToken || ""
        },
        body: JSON.stringify({ email, password })
      });

      const data = await res.json().catch(() => null);

      if (!res.ok) {
        throw new Error(data?.error || data?.message || "Invalid email or password");
      }

      showAlert("Login successful! Redirecting to dashboard...", "success");
      setTimeout(() => {
        window.location.href = "/";
      }, 400);
    } catch (err) {
      showAlert(err.message);
      setLoading(btn, false, "Sign in to FoodFlow →");
    }
  });
}

// Setup Sign Up Form
const signupForm = document.getElementById("signup-form");
if (signupForm) {
  const pwInput = document.getElementById("signup-password");
  const confirmInput = document.getElementById("signup-confirm-password");
  const strengthBar = document.getElementById("strength-bar");
  const strengthText = document.getElementById("strength-text");

  if (pwInput && strengthBar && strengthText) {
    pwInput.addEventListener("input", () => {
      const val = pwInput.value;
      let score = 0;
      if (val.length >= 8) score++;
      if (/[A-Z]/.test(val) || /[0-9]/.test(val)) score++;
      if (/[^A-Za-z0-9]/.test(val) && val.length >= 10) score++;

      strengthBar.className = "strength-bar";
      if (val.length === 0) {
        strengthBar.style.width = "0%";
        strengthText.textContent = "Minimum 8 characters";
        strengthText.style.color = "#7d8982";
      } else if (val.length < 8) {
        strengthBar.classList.add("weak");
        strengthText.textContent = "Too short (min 8 characters)";
        strengthText.style.color = "#bd554b";
      } else if (score === 1) {
        strengthBar.classList.add("medium");
        strengthText.textContent = "Fair password";
        strengthText.style.color = "#c77a42";
      } else {
        strengthBar.classList.add("strong");
        strengthText.textContent = "Strong password";
        strengthText.style.color = "#2d694b";
      }
    });
  }

  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearAlert();
    const btn = document.getElementById("signup-submit-btn");
    const fullName = document.getElementById("signup-name").value.trim();
    const email = document.getElementById("signup-email").value.trim();
    const phone = document.getElementById("signup-phone").value.trim();
    const password = pwInput.value;
    const confirmPassword = confirmInput.value;

    if (password.length < 8) {
      showAlert("Password must be at least 8 characters long.");
      pwInput.focus();
      return;
    }

    if (password !== confirmPassword) {
      showAlert("Passwords do not match. Please re-enter.");
      confirmInput.focus();
      return;
    }

    setLoading(btn, true, "Create operator account →");

    try {
      const csrfToken = await fetchCsrfToken();
      const res = await fetch("/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfToken || ""
        },
        body: JSON.stringify({ fullName, email, phone, password })
      });

      const data = await res.json().catch(() => null);

      if (!res.ok) {
        throw new Error(data?.error || data?.message || "Registration failed. Please try again.");
      }

      showAlert("Account created! Redirecting to login...", "success");
      setTimeout(() => {
        window.location.href = `/login.html?registered=true&email=${encodeURIComponent(email)}`;
      }, 500);
    } catch (err) {
      showAlert(err.message);
      setLoading(btn, false, "Create operator account →");
    }
  });
}
