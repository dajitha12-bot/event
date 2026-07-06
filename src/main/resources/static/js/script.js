// Event Management System Client-Side Interactivity

document.addEventListener('DOMContentLoaded', () => {
    // Auto-fade dismissible alerts after 4 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.classList.remove('show');
            setTimeout(() => {
                alert.remove();
            }, 300);
        }, 4000);
    });

    // Handle delete confirmations dynamically if data attributes are present
    const deleteButtons = document.querySelectorAll('.btn-confirm-delete');
    deleteButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            const eventTitle = button.getAttribute('data-event-title') || 'this event';
            const confirmed = confirm(`Are you sure you want to delete "${eventTitle}"? This action cannot be undone.`);
            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
});
