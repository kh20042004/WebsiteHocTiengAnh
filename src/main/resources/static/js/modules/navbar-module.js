/**
 * Module quản lý Navigation Bar
 * Xử lý các chức năng liên quan đến thanh điều hướng
 * 
 * @author English 12 Smart Team
 * @version 1.0.0
 */

// ============================================
// NAVBAR MODULE - QUẢN LÝ THANH ĐIỀU HƯỚNG
// ============================================

const NavbarModule = {
    /**
     * Khởi tạo toàn bộ chức năng của navbar
     * Bao gồm: scroll effect, mobile menu, smooth scrolling
     */
    init() {
        console.log('🚀 [Navbar Module] Đang khởi tạo...');
        this.initScrollEffect();
        this.initMobileMenu();
        this.initSmoothScrolling();
        this.initActiveNavHighlight();
        console.log('✅ [Navbar Module] Khởi tạo thành công!');
    },

    /**
     * Tạo hiệu ứng glass và shadow cho navbar khi scroll
     * Thay đổi màu nền và độ mờ dựa trên vị trí scroll
     */
    initScrollEffect() {
        const navbar = document.getElementById('navbar');
        
        if (!navbar) {
            console.warn('⚠️ [Navbar Module] Không tìm thấy element #navbar');
            return;
        }

        let lastScrollY = 0;
        let isScrolling = false;

        // Sử dụng requestAnimationFrame để tối ưu hiệu năng
        const handleScroll = () => {
            const currentScrollY = window.scrollY;
            
            if (currentScrollY > 50) {
                // Khi scroll xuống hơn 50px
                navbar.classList.add('navbar-scrolled');
                navbar.classList.remove('navbar-top');
            } else {
                // Khi ở đầu trang
                navbar.classList.remove('navbar-scrolled');
                navbar.classList.add('navbar-top');
            }

            // Hiệu ứng ẩn/hiện navbar khi scroll
            if (currentScrollY > lastScrollY && currentScrollY > 100) {
                // Ẩn navbar khi scroll xuống
                navbar.classList.add('navbar-hidden');
            } else {
                // Hiện navbar khi scroll lên
                navbar.classList.remove('navbar-hidden');
            }

            lastScrollY = currentScrollY;
            isScrolling = false;
        };

        // Throttle scroll event cho hiệu năng tốt hơn
        window.addEventListener('scroll', () => {
            if (!isScrolling) {
                requestAnimationFrame(handleScroll);
                isScrolling = true;
            }
        }, { passive: true });

        console.log('✅ [Navbar Module] Scroll effect đã được khởi tạo');
    },

    /**
     * Quản lý menu trên thiết bị di động
     * Xử lý việc mở/đóng menu mobile và các tương tác
     */
    initMobileMenu() {
        const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
        const mobileMenu = document.getElementById('mobile-menu');
        const mobileMenuIcon = mobileMenuToggle?.querySelector('iconify-icon');
        
        if (!mobileMenuToggle || !mobileMenu) {
            console.warn('⚠️ [Navbar Module] Không tìm thấy mobile menu elements');
            return;
        }

        let isMenuOpen = false;

        /**
         * Toggle trạng thái menu mobile
         */
        const toggleMenu = () => {
            isMenuOpen = !isMenuOpen;
            
            if (isMenuOpen) {
                // Mở menu
                mobileMenu.classList.remove('hidden');
                mobileMenu.classList.add('mobile-menu-open');
                
                // Thay đổi icon thành X
                if (mobileMenuIcon) {
                    mobileMenuIcon.setAttribute('icon', 'solar:close-linear');
                }
                
                // Khóa scroll của body
                document.body.classList.add('overflow-hidden');
                
                console.log('📱 [Navbar Module] Mobile menu đã mở');
            } else {
                // Đóng menu
                mobileMenu.classList.add('hidden');
                mobileMenu.classList.remove('mobile-menu-open');
                
                // Thay đổi icon về hamburger
                if (mobileMenuIcon) {
                    mobileMenuIcon.setAttribute('icon', 'solar:hamburger-menu-linear');
                }
                
                // Bỏ khóa scroll của body
                document.body.classList.remove('overflow-hidden');
                
                console.log('📱 [Navbar Module] Mobile menu đã đóng');
            }
        };

        // Sự kiện click vào nút toggle
        mobileMenuToggle.addEventListener('click', (e) => {
            e.preventDefault();
            toggleMenu();
        });

        // Đóng menu khi click vào các link
        const mobileMenuLinks = mobileMenu.querySelectorAll('a');
        mobileMenuLinks.forEach(link => {
            link.addEventListener('click', () => {
                if (isMenuOpen) {
                    toggleMenu();
                }
            });
        });

        // Đóng menu khi click ra ngoài (trên desktop)
        document.addEventListener('click', (e) => {
            if (isMenuOpen && !mobileMenu.contains(e.target) && !mobileMenuToggle.contains(e.target)) {
                toggleMenu();
            }
        });

        // Đóng menu khi resize màn hình về desktop
        window.addEventListener('resize', () => {
            if (window.innerWidth >= 768 && isMenuOpen) {
                toggleMenu();
            }
        });

        console.log('✅ [Navbar Module] Mobile menu đã được khởi tạo');
    },

    /**
     * Smooth scrolling cho các internal links
     * Tạo hiệu ứng cuộn mượt khi click vào anchor links
     */
    initSmoothScrolling() {
        // Lấy tất cả các links có href bắt đầu bằng #
        const anchorLinks = document.querySelectorAll('a[href^="#"]');
        
        anchorLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                const href = link.getAttribute('href');
                
                // Bỏ qua nếu chỉ là # hoặc #top
                if (href === '#' || href === '#top') {
                    e.preventDefault();
                    window.scrollTo({
                        top: 0,
                        behavior: 'smooth'
                    });
                    return;
                }

                const targetElement = document.querySelector(href);
                
                if (targetElement) {
                    e.preventDefault();
                    
                    // Tính toán offset để tránh bị navbar che khuất
                    const navbar = document.getElementById('navbar');
                    const navbarHeight = navbar ? navbar.offsetHeight : 0;
                    const targetPosition = targetElement.offsetTop - navbarHeight - 20; // Thêm 20px padding
                    
                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });
                    
                    console.log(`🔗 [Navbar Module] Smooth scroll đến: ${href}`);
                }
            });
        });

        console.log('✅ [Navbar Module] Smooth scrolling đã được khởi tạo');
    },

    /**
     * Highlight navigation item đang active dựa trên section hiện tại
     * Cập nhật trạng thái active của menu items khi scroll
     */
    initActiveNavHighlight() {
        const navLinks = document.querySelectorAll('nav a[href^="#"]');
        const sections = document.querySelectorAll('section[id]');
        
        if (sections.length === 0) {
            console.log('ℹ️ [Navbar Module] Không có sections để highlight');
            return;
        }

        /**
         * Cập nhật active state dựa trên vị trí scroll
         */
        const updateActiveNav = () => {
            const scrollPosition = window.scrollY + 100; // Offset 100px
            
            let currentSection = '';
            
            // Tìm section hiện tại
            sections.forEach(section => {
                const sectionTop = section.offsetTop;
                const sectionHeight = section.offsetHeight;
                
                if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                    currentSection = section.getAttribute('id');
                }
            });
            
            // Cập nhật active class cho nav links
            navLinks.forEach(link => {
                const href = link.getAttribute('href').substring(1); // Bỏ ký tự #
                
                if (href === currentSection) {
                    link.classList.add('nav-active');
                } else {
                    link.classList.remove('nav-active');
                }
            });
        };

        // Sử dụng Intersection Observer để tối ưu hiệu năng
        const observerOptions = {
            rootMargin: '-50px 0px -50px 0px',
            threshold: 0.1
        };

        const observer = new IntersectionObserver((entries) => {
            updateActiveNav();
        }, observerOptions);

        // Observe tất cả sections
        sections.forEach(section => {
            observer.observe(section);
        });

        console.log('✅ [Navbar Module] Active nav highlight đã được khởi tạo');
    }
};

// Export module để sử dụng ở nơi khác
if (typeof module !== 'undefined' && module.exports) {
    module.exports = NavbarModule;
}