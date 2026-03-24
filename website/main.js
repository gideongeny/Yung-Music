document.addEventListener('DOMContentLoaded', () => {
    // 1. Smooth Scroll Navigation
    const navbar = document.getElementById('navbar');
    if (navbar) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    }

    // 2. Intersection Observer for stagger reveal
    // We run this FIRST to ensure content is visible even if other scripts fail
    try {
        const revealOptions = { threshold: 0.1, rootMargin: '0px 0px -50px 0px' };
        const revealObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                    // Stop observing once revealed to save resources
                    revealObserver.unobserve(entry.target);
                }
            });
        }, revealOptions);

        const animateElements = document.querySelectorAll('[data-animate]');
        if (animateElements.length > 0) {
            animateElements.forEach(el => revealObserver.observe(el));
        } else {
            console.warn('No elements found with data-animate attribute');
        }
    } catch (e) {
        console.error('Observer failed:', e);
        // Fallback: reveal everything if observer fails
        document.querySelectorAll('[data-animate]').forEach(el => {
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        });
    }

    // 3. Hover Mouse Effect for Feature Cards (Spotlight)
    const cards = document.querySelectorAll('.f-card');
    if (cards.length > 0) {
        cards.forEach(card => {
            card.addEventListener('mousemove', (e) => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                card.style.setProperty('--mx', `${x}px`);
                card.style.setProperty('--my', `${y}px`);
            });
        });
    }

    // 4. Hero Visual 3D Interaction
    try {
        const container = document.querySelector('.hero');
        const s25 = document.querySelector('.mockup-s25');
        const pixel = document.querySelector('.mockup-pixel');
        const floaters = document.querySelectorAll('.floater');

        if (container && s25 && pixel && window.innerWidth > 1024) {
            container.addEventListener('mousemove', (e) => {
                const { clientX, clientY } = e;
                const xRotation = (clientY / window.innerHeight - 0.5) * -15;
                const yRotation = (clientX / window.innerWidth - 0.5) * 15;

                s25.style.transform = `translateX(-60px) rotateY(${25 + yRotation}deg) rotateX(${10 + xRotation}deg) translateZ(30px)`;
                pixel.style.transform = `translateX(120px) translateZ(-100px) rotateY(${-20 + (yRotation * -0.5)}deg) rotateX(${5 + (xRotation * -0.5)}deg)`;

                if (floaters.length > 0) {
                    floaters.forEach((floater, index) => {
                        const depth = (index + 1) * 30;
                        floater.style.transform = `translate(${(clientX - window.innerWidth / 2) / depth}px, ${(clientY - window.innerHeight / 2) / depth}px)`;
                    });
                }
            });

            container.addEventListener('mouseleave', () => {
                s25.style.transform = `translateX(-60px) rotateY(25deg) rotateX(10deg)`;
                pixel.style.transform = `translateX(120px) translateZ(-100px) rotateY(-20deg) rotateX(5deg)`;
                if (floaters.length > 0) {
                    floaters.forEach(f => f.style.transform = `translate(0, 0)`);
                }
            });
        }
    } catch (e) {
        console.error('3D Interaction failed:', e);
    }

    // 5. Magnetic Secondary Button Effect
    const btnSecondary = document.querySelector('.btn-secondary');
    if (btnSecondary) {
        btnSecondary.addEventListener('mousemove', (e) => {
            const rect = btnSecondary.getBoundingClientRect();
            const x = e.clientX - rect.left - rect.width / 2;
            const y = e.clientY - rect.top - rect.height / 2;
            btnSecondary.style.transform = `translate(${x * 0.3}px, ${y * 0.3}px)`;
        });
        btnSecondary.addEventListener('mouseleave', () => {
            btnSecondary.style.transform = 'translate(0, 0)';
        });
    }
});
