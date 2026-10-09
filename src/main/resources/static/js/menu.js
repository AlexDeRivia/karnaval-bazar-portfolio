const openMenu = document.querySelector('#open-menu');
const closeMenu = document.querySelector('#close-menu');
const sidebar = document.querySelector('.management-sidebar');

if (openMenu && closeMenu && sidebar) {
    openMenu.addEventListener('click', () => sidebar.classList.add('aside-visible'));
    closeMenu.addEventListener('click', () => sidebar.classList.remove('aside-visible'));
    sidebar.querySelectorAll('nav a').forEach(link => {
        link.addEventListener('click', () => sidebar.classList.remove('aside-visible'));
    });
}

function updateCurrentSection() {
    document.querySelectorAll('.management-nav a').forEach(link => {
        const destination = new URL(link.href, window.location.href);
        const currentPath = window.location.pathname;
        const sameSection = destination.hash
            ? destination.pathname === currentPath && destination.hash === (window.location.hash || '#resumen')
            : currentPath === destination.pathname || currentPath.startsWith(destination.pathname + '/');
        if (sameSection) link.setAttribute('aria-current', 'page');
        else link.removeAttribute('aria-current');
    });
}

updateCurrentSection();
window.addEventListener('hashchange', updateCurrentSection);
