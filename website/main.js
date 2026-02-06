document.addEventListener('DOMContentLoaded', () => {
    // Intersection Observer for scroll animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                // Optional: stop observing once animation is triggered
                // observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Track all elements with animation classes
    const animatedElements = document.querySelectorAll('.animate-up, .animate-fade-in');
    animatedElements.forEach(el => observer.observe(el));

    // Smoothing the transition for the phone mockup rotation on move
    const phone = document.querySelector('.phone-s25-ultra');
    const hero = document.querySelector('.hero');

    if (phone && window.innerWidth > 968) {
        hero.addEventListener('mousemove', (e) => {
            const { clientX, clientY } = e;
            const { innerWidth, innerHeight } = window;

            // Calculate rotation based on mouse position relative to center
            const xRotation = ((clientY / innerHeight) - 0.5) * -20;
            const yRotation = ((clientX / innerWidth) - 0.5) * 20;

            phone.style.transform = `rotateX(${10 + xRotation}deg) rotateY(${-15 + yRotation}deg)`;
        });

        hero.addEventListener('mouseleave', () => {
            phone.style.transform = `rotateX(10deg) rotateY(-15deg)`;
        });
    }

    // Direct Download functionality check
    const downloadBtns = document.querySelectorAll('a[href$=".apk"]');
    downloadBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            console.log('Initiating direct download...');
            // The browser handles direct apk downloads automatically if the link is direct
        });
    });
});
