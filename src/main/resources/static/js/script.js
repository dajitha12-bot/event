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

    // Handle delete confirmations dynamically
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

    // Real-time client-side search and filtering for events list
    const searchInput = document.getElementById('eventSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keyup', () => {
            const filter = searchInput.value.toLowerCase();
            const rows = document.querySelectorAll('.event-row');
            
            rows.forEach(row => {
                const titleNode = row.querySelector('.search-title');
                const venueNode = row.querySelector('.search-venue');
                
                const title = titleNode ? titleNode.textContent.toLowerCase() : '';
                const venue = venueNode ? venueNode.textContent.toLowerCase() : '';
                
                if (title.includes(filter) || venue.includes(filter)) {
                    row.style.setProperty('display', '', 'important');
                } else {
                    row.style.setProperty('display', 'none', 'important');
                }
            });
        });
    }
});
