document.addEventListener('DOMContentLoaded', () => {

    // ========================================
    // 1. PARTICLE BACKGROUND SYSTEM
    // ========================================
    const canvas = document.getElementById('particle-canvas');
    const ctx = canvas ? canvas.getContext('2d') : null;

    if (canvas && ctx) {
        let particles = [];
        let mouse = { x: null, y: null, radius: 150 };

        // Set canvas size
        function resizeCanvas() {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
        }
        resizeCanvas();
        window.addEventListener('resize', resizeCanvas);

        // Particle class
        class Particle {
            constructor() {
                this.x = Math.random() * canvas.width;
                this.y = Math.random() * canvas.height;
                this.size = Math.random() * 3 + 1;
                this.speedX = Math.random() * 1 - 0.5;
                this.speedY = Math.random() * 1 - 0.5;
                this.opacity = Math.random() * 0.5 + 0.2;
            }

            update() {
                this.x += this.speedX;
                this.y += this.speedY;

                // Mouse interaction
                if (mouse.x && mouse.y) {
                    const dx = mouse.x - this.x;
                    const dy = mouse.y - this.y;
                    const distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < mouse.radius) {
                        const force = (mouse.radius - distance) / mouse.radius;
                        const angle = Math.atan2(dy, dx);
                        this.x -= Math.cos(angle) * force * 2;
                        this.y -= Math.sin(angle) * force * 2;
                    }
                }

                // Wrap around screen
                if (this.x > canvas.width) this.x = 0;
                if (this.x < 0) this.x = canvas.width;
                if (this.y > canvas.height) this.y = 0;
                if (this.y < 0) this.y = canvas.height;
            }

            draw() {
                ctx.fillStyle = `rgba(138, 43, 226, ${this.opacity})`;
                ctx.beginPath();
                ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
                ctx.fill();
            }
        }

        // Initialize particles
        function initParticles() {
            particles = [];
            const numberOfParticles = Math.min(100, Math.floor((canvas.width * canvas.height) / 10000));
            for (let i = 0; i < numberOfParticles; i++) {
                particles.push(new Particle());
            }
        }
        initParticles();
        window.addEventListener('resize', initParticles);

        // Mouse move event
        window.addEventListener('mousemove', (e) => {
            mouse.x = e.x;
            mouse.y = e.y;
        });

        window.addEventListener('mouseout', () => {
            mouse.x = null;
            mouse.y = null;
        });

        // Animation loop
        function animateParticles() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            particles.forEach(particle => {
                particle.update();
                particle.draw();
            });

            // Connect nearby particles
            for (let i = 0; i < particles.length; i++) {
                for (let j = i + 1; j < particles.length; j++) {
                    const dx = particles[i].x - particles[j].x;
                    const dy = particles[i].y - particles[j].y;
                    const distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 120) {
                        ctx.strokeStyle = `rgba(138, 43, 226, ${0.2 * (1 - distance / 120)})`;
                        ctx.lineWidth = 1;
                        ctx.beginPath();
                        ctx.moveTo(particles[i].x, particles[i].y);
                        ctx.lineTo(particles[j].x, particles[j].y);
                        ctx.stroke();
                    }
                }
            }

            requestAnimationFrame(animateParticles);
        }
        animateParticles();
    }

    // ========================================
    // 2. LOADING OVERLAY
    // ========================================
    const loadingOverlay = document.getElementById('loading-overlay');
    window.addEventListener('load', () => {
        setTimeout(() => {
            if (loadingOverlay) {
                loadingOverlay.classList.add('hidden');
            }
        }, 500);
    });

    // ========================================
    // 3. SCROLL REVEAL ANIMATIONS
    // ========================================
    const revealElements = document.querySelectorAll('[data-scroll-reveal]');

    const revealObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('revealed');
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });

    revealElements.forEach(el => revealObserver.observe(el));

    // ========================================
    // 4. 3D HERO IMAGE MOUSE TRACKING
    // ========================================
    const heroMockup = document.getElementById('hero-mockup');
    const mockupContainer = document.querySelector('.mockup-3d-container');

    if (heroMockup && mockupContainer) {
        mockupContainer.addEventListener('mousemove', (e) => {
            const rect = mockupContainer.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const rotateX = ((y - centerY) / centerY) * 15;
            const rotateY = ((x - centerX) / centerX) * -15;

            mockupContainer.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
        });

        mockupContainer.addEventListener('mouseleave', () => {
            mockupContainer.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg)';
        });
    }

    // ========================================
    // 5. 3D GALLERY CAROUSEL
    // ========================================
    const galleryTrack = document.querySelector('.gallery-3d-track');
    const galleryItems = document.querySelectorAll('.gallery-item');
    const prevBtn = document.getElementById('gallery-prev');
    const nextBtn = document.getElementById('gallery-next');

    if (galleryTrack && galleryItems.length > 0) {
        let currentIndex = 0;
        const itemWidth = galleryItems[0].offsetWidth + 30; // width + gap

        function updateGallery() {
            const offset = -currentIndex * itemWidth;
            galleryTrack.style.transform = `translateX(${offset}px)`;
        }

        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                currentIndex = Math.max(0, currentIndex - 1);
                updateGallery();
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                currentIndex = Math.min(galleryItems.length - 1, currentIndex + 1);
                updateGallery();
            });
        }

        // Touch/swipe support
        let touchStartX = 0;
        let touchEndX = 0;

        galleryTrack.addEventListener('touchstart', (e) => {
            touchStartX = e.changedTouches[0].screenX;
        });

        galleryTrack.addEventListener('touchend', (e) => {
            touchEndX = e.changedTouches[0].screenX;
            handleSwipe();
        });

        function handleSwipe() {
            if (touchEndX < touchStartX - 50) {
                // Swipe left
                currentIndex = Math.min(galleryItems.length - 1, currentIndex + 1);
                updateGallery();
            }
            if (touchEndX > touchStartX + 50) {
                // Swipe right
                currentIndex = Math.max(0, currentIndex - 1);
                updateGallery();
            }
        }

        // Keyboard navigation
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowLeft') {
                currentIndex = Math.max(0, currentIndex - 1);
                updateGallery();
            } else if (e.key === 'ArrowRight') {
                currentIndex = Math.min(galleryItems.length - 1, currentIndex + 1);
                updateGallery();
            }
        });
    }

    // ========================================
    // 6. PARALLAX SCROLL EFFECT
    // ========================================
    let lastScrollY = window.scrollY;

    function parallaxScroll() {
        const scrollY = window.scrollY;

        // Parallax for hero section
        const heroContent = document.querySelector('.hero-content');
        const heroImage = document.querySelector('.hero-image');

        if (heroContent && heroImage) {
            heroContent.style.transform = `translateY(${scrollY * 0.3}px)`;
            heroImage.style.transform = `translateY(${scrollY * 0.2}px)`;
        }

        lastScrollY = scrollY;
    }

    let ticking = false;
    window.addEventListener('scroll', () => {
        if (!ticking) {
            window.requestAnimationFrame(() => {
                parallaxScroll();
                ticking = false;
            });
            ticking = true;
        }
    });

    // ========================================
    // 7. MOBILE MENU
    // ========================================
    const mobileMenu = document.getElementById('mobile-menu');
    const navLinks = document.querySelector('.nav-links');

    if (mobileMenu) {
        mobileMenu.addEventListener('click', () => navLinks.classList.toggle('active'));
    }

    // ========================================
    // 8. FAQ ACCORDION
    // ========================================
    const accordionHeaders = document.querySelectorAll('.accordion-header');
    accordionHeaders.forEach(header => {
        header.addEventListener('click', () => {
            const content = header.nextElementSibling;
            document.querySelectorAll('.accordion-content').forEach(item => {
                if (item !== content) item.style.maxHeight = null;
            });
            content.style.maxHeight = content.style.maxHeight ? null : content.scrollHeight + "px";
        });
    });

    // ========================================
    // 9. SMOOTH SCROLL
    // ========================================
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            e.preventDefault();
            if (navLinks) navLinks.classList.remove('active');
            const target = document.querySelector(targetId);
            if (target) target.scrollIntoView({ behavior: 'smooth' });
        });
    });

    // ========================================
    // 10. THEME MANAGEMENT
    // ========================================
    const themeBtn = document.getElementById('theme-toggle');
    const themeText = document.getElementById('theme-text');
    const themeIcon = themeBtn ? themeBtn.querySelector('i') : null;
    const htmlElement = document.documentElement;

    const getSystemTheme = () => window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';

    function updateThemeUI(mode, actualTheme) {
        htmlElement.setAttribute('data-theme', actualTheme);

        if (themeText) themeText.innerText = mode;

        if (themeIcon) {
            if (mode === 'auto') {
                themeIcon.className = 'fas fa-magic';
            } else {
                themeIcon.className = actualTheme === 'light' ? 'fas fa-sun' : 'fas fa-moon';
            }
        }
    }

    function applyMode(mode) {
        let themeToApply = mode;
        if (mode === 'auto') {
            themeToApply = getSystemTheme();
        }

        updateThemeUI(mode, themeToApply);
        localStorage.setItem('theme-mode', mode);
    }

    const savedMode = localStorage.getItem('theme-mode') || 'auto';
    applyMode(savedMode);

    if (themeBtn) {
        themeBtn.addEventListener('click', () => {
            const currentMode = localStorage.getItem('theme-mode') || 'auto';
            let nextMode;

            if (currentMode === 'auto') nextMode = 'light';
            else if (currentMode === 'light') nextMode = 'dark';
            else nextMode = 'auto';

            applyMode(nextMode);
        });
    }

    window.matchMedia('(prefers-color-scheme: light)').addEventListener('change', (e) => {
        if (localStorage.getItem('theme-mode') === 'auto') {
            updateThemeUI('auto', e.matches ? 'light' : 'dark');
        }
    });

    // ========================================
    // 11. FETCH VERSION FROM GITHUB
    // ========================================
    const versionBadge = document.getElementById('version-badge');
    if (versionBadge) {
        fetch('https://api.github.com/repos/gideongeny/Yung-Music/releases/latest', {
            method: 'GET',
            headers: {
                'Accept': 'application/vnd.github.v3+json'
            }
        })
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(data => {
                if (data && data.tag_name) {
                    const version = data.tag_name;
                    const isPrerelease = data.prerelease;
                    versionBadge.textContent = `${version} ${isPrerelease ? 'Beta' : 'Stable'}`;
                }
            })
            .catch(error => {
                console.log('Version fetch error:', error);
            });
    }

    // ========================================
    // 12. PERFORMANCE OPTIMIZATION
    // ========================================

    // Debounce function
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Reduced motion support
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

    if (prefersReducedMotion.matches) {
        // Disable particle system for reduced motion
        if (canvas) canvas.style.display = 'none';
    }

    // ========================================
    // 13. ENHANCED CARD 3D EFFECTS
    // ========================================
    const featureCards = document.querySelectorAll('.feature-card');

    featureCards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const rotateX = ((y - centerY) / centerY) * 5;
            const rotateY = ((x - centerX) / centerX) * 5;

            card.style.transform = `translateY(-10px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0) rotateX(0deg) rotateY(0deg)';
        });
    });

    // ========================================
    // 14. NAVBAR SCROLL EFFECT
    // ========================================
    const navbar = document.querySelector('.navbar');
    let lastScroll = 0;

    window.addEventListener('scroll', debounce(() => {
        const currentScroll = window.pageYOffset;

        if (currentScroll > 100) {
            navbar.style.boxShadow = '0 4px 30px rgba(0, 0, 0, 0.3)';
        } else {
            navbar.style.boxShadow = '0 4px 30px rgba(0, 0, 0, 0.1)';
        }

        lastScroll = currentScroll;
    }, 10));

    console.log('🎨 YungMusic - World-Class Website Loaded Successfully!');
});