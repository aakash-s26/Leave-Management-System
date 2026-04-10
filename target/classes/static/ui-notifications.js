(function() {
    const STYLE_ID = 'leavepal-ui-notification-style';
    const TOAST_STACK_ID = 'leavepal-toast-stack';

    function ensureStyles() {
        if (document.getElementById(STYLE_ID)) {
            return;
        }

        const style = document.createElement('style');
        style.id = STYLE_ID;
        style.textContent = `
            .lp-toast-stack {
                position: fixed;
                top: 18px;
                right: 18px;
                z-index: 5200;
                display: flex;
                flex-direction: column;
                gap: 10px;
                width: min(360px, calc(100vw - 24px));
                pointer-events: none;
            }
            .lp-toast {
                pointer-events: auto;
                display: grid;
                grid-template-columns: 1fr auto;
                gap: 10px;
                align-items: start;
                background: #1f1f2b;
                color: #f8f8f8;
                border: 1px solid rgba(212, 175, 55, 0.35);
                border-left: 4px solid #d4af37;
                border-radius: 10px;
                box-shadow: 0 12px 24px rgba(0, 0, 0, 0.28);
                padding: 12px 12px 12px 14px;
                transform: translateX(20px);
                opacity: 0;
                animation: lp-toast-in 200ms ease forwards;
            }
            .lp-toast-success { border-left-color: #1dbf73; }
            .lp-toast-warning { border-left-color: #ffb020; }
            .lp-toast-error { border-left-color: #e5484d; }
            .lp-toast-message {
                font-size: 13px;
                line-height: 1.45;
                margin: 0;
                white-space: pre-line;
            }
            .lp-toast-close {
                border: 0;
                background: transparent;
                color: #bfc0cf;
                font-size: 16px;
                line-height: 1;
                cursor: pointer;
                padding: 2px;
            }
            .lp-toast-close:hover { color: #ffffff; }
            .lp-toast-out {
                animation: lp-toast-out 180ms ease forwards;
            }
            .lp-confirm-overlay {
                position: fixed;
                inset: 0;
                background: rgba(0, 0, 0, 0.52);
                z-index: 5100;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 16px;
            }
            .lp-confirm-card {
                width: min(460px, 100%);
                background: #2b2b3a;
                color: #f4f4f8;
                border: 1px solid rgba(212, 175, 55, 0.35);
                border-radius: 14px;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.35);
                padding: 22px;
            }
            .lp-confirm-title {
                margin: 0 0 10px;
                font-size: 19px;
                font-weight: 700;
                color: #f8f8f8;
            }
            .lp-confirm-message {
                margin: 0;
                font-size: 14px;
                line-height: 1.6;
                color: #d9d9e4;
                white-space: pre-line;
            }
            .lp-confirm-actions {
                margin-top: 20px;
                display: flex;
                justify-content: flex-end;
                gap: 10px;
            }
            .lp-confirm-code {
                margin-top: 14px;
                padding: 12px 14px;
                border-radius: 10px;
                background: rgba(255, 255, 255, 0.08);
                border: 1px solid rgba(255, 255, 255, 0.12);
                font-size: 14px;
                line-height: 1.6;
                color: #f8f8f8;
                white-space: pre-line;
                word-break: break-word;
            }
            .lp-confirm-success {
                margin-top: 14px;
                padding: 12px 14px;
                border-radius: 10px;
                background: rgba(29, 191, 115, 0.14);
                border: 1px solid rgba(29, 191, 115, 0.35);
                color: #d8f7e7;
                font-size: 14px;
                font-weight: 600;
                line-height: 1.5;
            }
            .lp-confirm-btn {
                border: 0;
                border-radius: 9px;
                padding: 10px 14px;
                font-size: 13px;
                font-weight: 700;
                cursor: pointer;
            }
            .lp-confirm-btn-cancel {
                background: rgba(255, 255, 255, 0.14);
                color: #ececf3;
            }
            .lp-confirm-btn-ok {
                background: #d4af37;
                color: #1a1a1a;
            }
            .lp-confirm-btn-ok.lp-danger {
                background: #e5484d;
                color: #ffffff;
            }
            @keyframes lp-toast-in {
                from { transform: translateX(20px); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes lp-toast-out {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(20px); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }

    function getToastStack() {
        ensureStyles();
        let stack = document.getElementById(TOAST_STACK_ID);
        if (!stack) {
            stack = document.createElement('div');
            stack.id = TOAST_STACK_ID;
            stack.className = 'lp-toast-stack';
            document.body.appendChild(stack);
        }
        return stack;
    }

    function removeToast(toast) {
        if (!toast || !toast.parentElement) {
            return;
        }
        toast.classList.add('lp-toast-out');
        setTimeout(function() {
            if (toast.parentElement) {
                toast.parentElement.removeChild(toast);
            }
        }, 180);
    }

    function notify(message, type, options) {
        if (!document.body) {
            return;
        }

        const config = options || {};
        const level = String(type || 'info').toLowerCase();
        const timeout = Number(config.timeoutMs || 3500);
        const stack = getToastStack();

        const toast = document.createElement('div');
        toast.className = 'lp-toast lp-toast-' + level;

        const content = document.createElement('p');
        content.className = 'lp-toast-message';
        content.textContent = String(message || 'Notification');

        const closeButton = document.createElement('button');
        closeButton.className = 'lp-toast-close';
        closeButton.type = 'button';
        closeButton.setAttribute('aria-label', 'Dismiss');
        closeButton.textContent = 'x';
        closeButton.addEventListener('click', function() {
            removeToast(toast);
        });

        toast.appendChild(content);
        toast.appendChild(closeButton);
        stack.appendChild(toast);

        if (timeout > 0) {
            setTimeout(function() {
                removeToast(toast);
            }, timeout);
        }
    }

    function showConfirmDialog(options) {
        ensureStyles();

        const config = options || {};
        const title = config.title || 'Confirm action';
        const message = config.message || 'Are you sure you want to continue?';
        const confirmText = config.confirmText || 'Confirm';
        const cancelText = config.cancelText || 'Cancel';
        const isDanger = !!config.danger;

        return new Promise(function(resolve) {
            const overlay = document.createElement('div');
            overlay.className = 'lp-confirm-overlay';

            const card = document.createElement('div');
            card.className = 'lp-confirm-card';

            const titleNode = document.createElement('h3');
            titleNode.className = 'lp-confirm-title';
            titleNode.textContent = title;

            const messageNode = document.createElement('p');
            messageNode.className = 'lp-confirm-message';
            messageNode.textContent = message;

            const actions = document.createElement('div');
            actions.className = 'lp-confirm-actions';

            const cancelBtn = document.createElement('button');
            cancelBtn.type = 'button';
            cancelBtn.className = 'lp-confirm-btn lp-confirm-btn-cancel';
            cancelBtn.textContent = cancelText;

            const okBtn = document.createElement('button');
            okBtn.type = 'button';
            okBtn.className = 'lp-confirm-btn lp-confirm-btn-ok' + (isDanger ? ' lp-danger' : '');
            okBtn.textContent = confirmText;

            function cleanup(result) {
                if (overlay.parentElement) {
                    overlay.parentElement.removeChild(overlay);
                }
                document.removeEventListener('keydown', onKeyDown);
                resolve(result);
            }

            function onKeyDown(event) {
                if (event.key === 'Escape') {
                    cleanup(false);
                }
            }

            cancelBtn.addEventListener('click', function() {
                cleanup(false);
            });

            okBtn.addEventListener('click', function() {
                cleanup(true);
            });

            overlay.addEventListener('click', function(event) {
                if (event.target === overlay) {
                    cleanup(false);
                }
            });

            document.addEventListener('keydown', onKeyDown);

            actions.appendChild(cancelBtn);
            actions.appendChild(okBtn);
            card.appendChild(titleNode);
            card.appendChild(messageNode);
            card.appendChild(actions);
            overlay.appendChild(card);
            document.body.appendChild(overlay);

            okBtn.focus();
        });
    }

    function showTemporaryPasswordDialog(options) {
        ensureStyles();

        const config = options || {};
        const username = config.username || 'User';
        const password = config.password || '';
        const successMessage = config.successMessage || 'Temporary password generated successfully.';

        return new Promise(function(resolve) {
            const overlay = document.createElement('div');
            overlay.className = 'lp-confirm-overlay';

            const card = document.createElement('div');
            card.className = 'lp-confirm-card';

            const titleNode = document.createElement('h3');
            titleNode.className = 'lp-confirm-title';
            titleNode.textContent = config.title || 'Temporary Password';

            const messageNode = document.createElement('p');
            messageNode.className = 'lp-confirm-message';
            messageNode.textContent = config.message || 'Share this temporary password securely with the employee.';

            const successNode = document.createElement('div');
            successNode.className = 'lp-confirm-success';
            successNode.textContent = successMessage;

            const codeNode = document.createElement('div');
            codeNode.className = 'lp-confirm-code';
            codeNode.textContent = 'User: ' + username + '\nPassword: ' + password;

            const actions = document.createElement('div');
            actions.className = 'lp-confirm-actions';

            const closeBtn = document.createElement('button');
            closeBtn.type = 'button';
            closeBtn.className = 'lp-confirm-btn lp-confirm-btn-cancel';
            closeBtn.textContent = config.closeText || 'Close';

            const copyBtn = document.createElement('button');
            copyBtn.type = 'button';
            copyBtn.className = 'lp-confirm-btn lp-confirm-btn-ok';
            copyBtn.textContent = config.copyText || 'Copy';

            function cleanup(result) {
                if (overlay.parentElement) {
                    overlay.parentElement.removeChild(overlay);
                }
                document.removeEventListener('keydown', onKeyDown);
                resolve(result);
            }

            function onKeyDown(event) {
                if (event.key === 'Escape') {
                    cleanup(false);
                }
            }

            closeBtn.addEventListener('click', function() {
                cleanup(false);
            });

            copyBtn.addEventListener('click', async function() {
                const text = 'User: ' + username + '\nPassword: ' + password;
                try {
                    if (navigator.clipboard && navigator.clipboard.writeText) {
                        await navigator.clipboard.writeText(text);
                    } else {
                        const textarea = document.createElement('textarea');
                        textarea.value = text;
                        textarea.setAttribute('readonly', 'readonly');
                        textarea.style.position = 'absolute';
                        textarea.style.left = '-9999px';
                        document.body.appendChild(textarea);
                        textarea.select();
                        document.execCommand('copy');
                        document.body.removeChild(textarea);
                    }
                    notify(config.copiedMessage || 'Copied', 'success');
                } catch (error) {
                    notify(config.copyFailedMessage || 'Unable to copy password', 'error');
                }
            });

            overlay.addEventListener('click', function(event) {
                if (event.target === overlay) {
                    cleanup(false);
                }
            });

            document.addEventListener('keydown', onKeyDown);

            actions.appendChild(closeBtn);
            actions.appendChild(copyBtn);
            card.appendChild(titleNode);
            card.appendChild(messageNode);
            card.appendChild(successNode);
            card.appendChild(codeNode);
            card.appendChild(actions);
            overlay.appendChild(card);
            document.body.appendChild(overlay);

            setTimeout(function() {
                notify(successMessage, 'success', { timeoutMs: 2200 });
            }, 80);

            copyBtn.focus();
        });
    }

    window.LeavePalUI = {
        notify: notify,
        confirm: showConfirmDialog,
        temporaryPassword: showTemporaryPasswordDialog
    };

    window.showTopRightNotification = notify;
    window.showActionConfirmDialog = showConfirmDialog;
    window.showTemporaryPasswordDialog = showTemporaryPasswordDialog;
})();
