/**
 * Truncates long Bootstrap pagination lists (Previous, 1 ... 8 9 [10] 11 12 ... 21, Next).
 * Applies to every ul.pagination rendered by Thymeleaf; no template changes required.
 */
document.addEventListener('DOMContentLoaded', function () {
    var WINDOW = 2; // pages shown on each side of the active page

    document.querySelectorAll('ul.pagination').forEach(function (ul) {
        var items = Array.prototype.slice.call(ul.children);
        if (items.length < 4) return;

        var pages = items.slice(1, items.length - 1); // drop Previous / Next
        if (pages.length <= 2 * WINDOW + 3) return;   // short enough, keep as is

        var activeIdx = pages.findIndex(function (li) { return li.classList.contains('active'); });
        if (activeIdx < 0) activeIdx = 0;

        var lastIdx = pages.length - 1;
        var lastWasHidden = false;

        pages.forEach(function (li, i) {
            var keep = i === 0 || i === lastIdx || Math.abs(i - activeIdx) <= WINDOW;
            if (keep) {
                lastWasHidden = false;
                return;
            }
            li.style.display = 'none';
            if (!lastWasHidden) {
                var gap = document.createElement('li');
                gap.className = 'page-item disabled';
                gap.innerHTML = '<span class="page-link">&hellip;</span>';
                ul.insertBefore(gap, li);
                lastWasHidden = true;
            }
        });
    });
});
