// Check stored preference or default to the currently active tab
document.addEventListener('DOMContentLoaded', function() {
    const notify = (message, type) => {
        if (window.showTopRightNotification) {
            window.showTopRightNotification(message, type);
            return;
        }
        console.error(message);
    };

    const activeTab = document.querySelector('.tab-btn.active');
    const savedTab = localStorage.getItem('loginRoleTab');
    const savedRole = localStorage.getItem('userRole');
    const preferredTab = savedTab || (savedRole && savedRole.toLowerCase() === 'admin' ? 'admin' : (activeTab ? activeTab.dataset.role : 'workforce'));
    setTheme(preferredTab);

    // Tab switching
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const role = this.dataset.role;
            setTheme(role);
            localStorage.setItem('loginRoleTab', role);

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
            this.style.opacity = type === 'text' ? '1' : '0.6';
        });
    }

    async function attemptLogin(username, password, role) {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password, role })
        });

        if (response.status === 428) {
            let details = {};
            try {
                details = await response.json();
            } catch (ignored) {
                details = {};
            }

            await showForceResetPasswordModal({
                username: details.username || username,
                currentPassword: password,
                role,
                notify,
                onSuccess: async (newPassword) => {
                    notify('Password reset complete. Logging you in...', 'success');
                    await attemptLogin(username, newPassword, role);
                }
            });
            return;
        }

        if (!response.ok) {
            const message = await response.text();
            notify(message || 'Login failed. Check your credentials.', 'error');
            return;
        }

        const user = await response.json();
        localStorage.setItem('loggedInUser', JSON.stringify(user));
        localStorage.setItem('userRole', user.role);

        const redirectUrl = user.redirectUrl || (user.role === 'admin' ? 'admin-dashboard.html' : 'dashboard.html');
        window.location.href = redirectUrl;
    }

    // Form submission
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const activeTab = document.querySelector('.tab-btn.active');
            const selectedRole = activeTab ? activeTab.dataset.role : 'workforce';

            try {
                await attemptLogin(username, password, selectedRole);
            } catch (error) {
                console.error(error);
                notify('Unable to connect to the authentication server.', 'error');
            }
        });
    }

    // Forgot password form submission
    const forgotForm = document.getElementById('forgotForm');
    if (forgotForm) {
        forgotForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();

            if (!email) {
                notify('Please enter your email ID.', 'warning');
                return;
            }

            try {
                const response = await fetch('/api/auth/forgot-password-request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ usernameOrEmail: email })
                });

                if (!response.ok) {
                    const message = await response.text();
                    notify(message || 'Unable to send password reset request.', 'error');
                    return;
                }

                notify('Password reset request sent. Admin can now see it in notifications and employee records.', 'success');
                forgotForm.reset();

                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1500);
            } catch (error) {
                console.error(error);
                notify('Unable to connect to the authentication server.', 'error');
            }
        });
    }
});

function showForceResetPasswordModal(config) {
    const username = config.username;
    const currentPassword = config.currentPassword;
    const notify = config.notify;
    const onSuccess = config.onSuccess;

    return new Promise((resolve) => {
        const overlay = document.createElement('div');
        overlay.className = 'force-reset-overlay';
        overlay.innerHTML = `
            <div class="force-reset-modal">
                <h3>Create New Password</h3>
                <p>You logged in with a temporary password. Please set a new password to continue.</p>
                <div class="force-reset-field">
                    <label for="forceResetNewPassword">NEW PASSWORD</label>
                    <input type="password" id="forceResetNewPassword" autocomplete="new-password" />
                </div>
                <div class="force-reset-field">
                    <label for="forceResetConfirmPassword">CONFIRM PASSWORD</label>
                    <input type="password" id="forceResetConfirmPassword" autocomplete="new-password" />
                </div>
                <div class="force-reset-actions">
                    <button type="button" class="force-reset-cancel">Cancel</button>
                    <button type="button" class="force-reset-submit">Reset Password</button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        const cancelBtn = overlay.querySelector('.force-reset-cancel');
        const submitBtn = overlay.querySelector('.force-reset-submit');
        const newPasswordInput = overlay.querySelector('#forceResetNewPassword');
        const confirmPasswordInput = overlay.querySelector('#forceResetConfirmPassword');

        function closeModal() {
            if (overlay.parentElement) {
                overlay.parentElement.removeChild(overlay);
            }
            resolve(false);
        }

        cancelBtn.addEventListener('click', closeModal);

        overlay.addEventListener('click', (event) => {
            if (event.target === overlay) {
                closeModal();
            }
        });

        submitBtn.addEventListener('click', async () => {
            const newPassword = newPasswordInput.value;
            const confirmPassword = confirmPasswordInput.value;

            if (!newPassword || !confirmPassword) {
                notify('Please enter both new and confirm password.', 'warning');
                return;
            }

            if (newPassword !== confirmPassword) {
                notify('New password and confirm password do not match.', 'error');
                return;
            }

            try {
                const response = await fetch('/api/auth/reset-temporary-password', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username,
                        currentPassword,
                        newPassword,
                        confirmPassword
                    })
                });

                if (!response.ok) {
                    const message = await response.text();
                    notify(message || 'Unable to reset password.', 'error');
                    return;
                }

                if (overlay.parentElement) {
                    overlay.parentElement.removeChild(overlay);
                }
                await onSuccess(newPassword);
                resolve(true);
            } catch (error) {
                console.error(error);
                notify('Unable to reset password at the moment.', 'error');
            }
        });
    });
}

// Theme switching function
function setTheme(role) {
    const body = document.body;
    const tabRole = role === 'admin' ? 'admin' : 'workforce';
    const forgotSection = document.getElementById('forgotPasswordSection');
    
    if (tabRole === 'admin') {
        body.classList.remove('employee-theme');
        body.classList.add('admin-theme');
        if (forgotSection) {
            forgotSection.style.display = 'none';
        }
    } else {
        body.classList.remove('admin-theme');
        body.classList.add('employee-theme');
        if (forgotSection) {
            forgotSection.style.display = 'block';
        }
    }
    
    // Update active tab
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        if (btn.dataset.role === tabRole) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}
