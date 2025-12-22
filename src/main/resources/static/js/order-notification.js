document.addEventListener("DOMContentLoaded", function () {

    function loadNewOrders() {
        fetch('/admin/new-order-count')
            .then(res => res.json())
            .then(count => {
                const badge = document.getElementById('orderBadge');
                if (!badge) return;

                if (count > 0) {
                    badge.textContent = count;
                    badge.classList.remove('d-none');
                } else {
                    badge.classList.add('d-none');
                }
            })
            .catch(err => console.error("Order badge error:", err));
    }

    loadNewOrders();
    setInterval(loadNewOrders, 5000);
});
