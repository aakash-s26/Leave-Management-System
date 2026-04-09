// Check stored preference or default to the currently active tab
document.addEventListener('DOMContentLoaded', function() {
    const activeTab = document.querySelector('.tab-btn.active');
    const savedRole = localStorage.getItem('userRole') || (activeTab ? activeTab.dataset.role : 'employee');
    setTheme(savedRole);
    
    // Tab switching
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const role = this.dataset.role;
            setTheme(role);
            localStorage.setItem('userRole', role);
            
            // Update active tab
            tabButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Password toggle
    const togglePassword = document.getElementById('togglePassword');
    if (togglePassword) {
        togglePassword.addEventListener('click', function(e) {
            e.preventDefault();
            const passwordInput = document.getElementById('password');
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Optional: Change icon
            this.style.opacity = type === 'text' ? '1' : '0.6';
        });
    }

    // Form submission
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const activeTab = document.querySelector('.tab-btn.active');
            const role = activeTab ? activeTab.dataset.role : (localStorage.getItem('userRole') || 'employee');

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password, role })
                });

                if (!response.ok) {
                    const message = await response.text();
                    alert(message || 'Login failed. Check your credentials.');
                    return;
                }

                const user = await response.json();
                localStorage.setItem('loggedInUser', JSON.stringify(user));
                localStorage.setItem('userRole', user.role);

                const redirectUrl = user.redirectUrl || (user.role === 'employee' ? 'dashboard.html' : 'admin-dashboard.html');
                window.location.href = redirectUrl;
            } catch (error) {
                console.error(error);
                alert('Unable to connect to the authentication server.');
            }
        });
    }

    // Forgot password form submission
    const forgotForm = document.getElementById('forgotForm');
    if (forgotForm) {
        forgotForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            
            // Simulate sending request
            alert(`Password reset request sent to the admin.\n\nThe admin will contact you shortly.`);
            
            // Reset form
            forgotForm.reset();
            
            // Redirect to login page
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1500);
        });
    }
});

// Theme switching function
function setTheme(role) {
    const body = document.body;
    
    if (role === 'admin') {
        body.classList.remove('employee-theme');
        body.classList.add('admin-theme');
    } else {
        body.classList.remove('admin-theme');
        body.classList.add('employee-theme');
    }
    
    // Update active tab
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        if (btn.dataset.role === role) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}
