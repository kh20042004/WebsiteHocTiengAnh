/**
 * ============================================
 * MODULE: BUTTON HANDLERS (button-handlers.js)
 * ============================================
 * 
 * CHỨC NĂNG:
 * - Xử lý tất cả button events trên trang chủ
 * - Redirect đến trang login/register
 * - Hiển thị video demo modal
 * - Xử lý mobile menu toggle
 * - Smooth scroll tới sections
 * - Tracking analytics cho button clicks
 * 
 * AUTHOR: English 12 Smart Team
 * VERSION: 1.0.0
 * ============================================
 */

// === ĐỊNH NGHĨA HẰNG SỐ ===
const ButtonHandlers = {
  // Các selector buttons
  SELECTORS: {
    // Navigation & Auth Buttons
    MOBILE_MENU_TOGGLE: '#mobile-menu-toggle',
    MOBILE_MENU: '#mobile-menu',
    
    // Hero Section
    HERO_CTA_BTN: 'a[href="/auth/register"].shadow-lg',
    DEMO_VIDEO_BTN: '#demo-video-btn',
    
    // Mobile Auth Links
    MOBILE_LOGIN_LINK: '#mobile-menu a[href="/auth/login"]',
    MOBILE_REGISTER_LINK: '#mobile-menu a[href="/auth/register"]',
    
    // Desktop Auth Links
    DESKTOP_LOGIN_LINK: '.hidden.md\\:flex a[href="/auth/login"]',
    DESKTOP_REGISTER_LINK: '.hidden.md\\:flex a[href="/auth/register"]',
    
    // Navigation Links
    NAV_HOME: 'nav a[href="#"]',
    NAV_FEATURES: 'nav a[href="#features"]',
    NAV_CONTACT: 'nav a[href="#footer"]',
    
    // Final CTA Button
    FINAL_CTA_BTN: 'a[href="/auth/register"].inline-block.px-8',
    
    // Counter elements
    COUNTERS: '.counter'
  },

  // Animation duration (ms)
  ANIMATION_DURATION: 300,

  // State tracking
  STATE: {
    mobileMenuOpen: false,
    videoModalOpen: false
  },

  // ==========================================
  // 01. MOBILE MENU HANDLER
  // ==========================================
  handleMobileMenuToggle() {
    const toggleBtn = document.querySelector(this.SELECTORS.MOBILE_MENU_TOGGLE);
    const mobileMenu = document.querySelector(this.SELECTORS.MOBILE_MENU);

    if (!toggleBtn || !mobileMenu) return;

    toggleBtn.addEventListener('click', () => {
      // Bật/Tắt state
      this.STATE.mobileMenuOpen = !this.STATE.mobileMenuOpen;

      // Toggle class 'hidden'
      if (this.STATE.mobileMenuOpen) {
        // Mở menu với animation
        mobileMenu.classList.remove('hidden');
        setTimeout(() => {
          mobileMenu.style.opacity = '1';
          mobileMenu.style.transform = 'translateY(0)';
        }, 10);
        
        // Change icon (hamburger → close)
        toggleBtn.innerHTML = '<iconify-icon icon="solar:close-circle-linear" width="24"></iconify-icon>';
      } else {
        // Đóng menu với animation
        mobileMenu.style.opacity = '0';
        mobileMenu.style.transform = 'translateY(-10px)';
        setTimeout(() => {
          mobileMenu.classList.add('hidden');
        }, this.ANIMATION_DURATION);
        
        // Change icon (close → hamburger)
        toggleBtn.innerHTML = '<iconify-icon icon="solar:hamburger-menu-linear" width="24"></iconify-icon>';
      }

      // Log tracking
      console.log('📱 Mobile menu:', this.STATE.mobileMenuOpen ? 'opened' : 'closed');
    });
  },

  // ==========================================
  // 02. MOBILE MENU LINKS HANDLER
  // ==========================================
  handleMobileMenuLinks() {
    // Khi click vào link trong mobile menu → đóng menu
    const mobileMenuLinks = document.querySelectorAll('#mobile-menu a, #mobile-menu button');
    
    mobileMenuLinks.forEach(link => {
      link.addEventListener('click', () => {
        // Đóng menu
        if (this.STATE.mobileMenuOpen) {
          const toggleBtn = document.querySelector(this.SELECTORS.MOBILE_MENU_TOGGLE);
          if (toggleBtn) {
            toggleBtn.click(); // Trigger click để toggle menu
          }
        }
      });
    });
  },

  // ==========================================
  // 03. SMOOTH SCROLL HANDLER
  // ==========================================
  handleSmoothScroll() {
    // Tất cả internal links (#sections)
    const internalLinks = document.querySelectorAll('a[href^="#"]');
    
    internalLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        const href = link.getAttribute('href');
        
        // Bỏ qua pure '#' links
        if (href === '#') {
          e.preventDefault();
          window.scrollTo({ top: 0, behavior: 'smooth' });
          return;
        }

        const target = document.querySelector(href);
        if (target) {
          e.preventDefault();
          
          // Tính toán offset từ navbar height (64px = h-16)
          const offsetTop = target.offsetTop - 64;
          
          // Smooth scroll
          window.scrollTo({
            top: offsetTop,
            behavior: 'smooth'
          });

          console.log(`🔗 Scrolling to: ${href}`);
        }
      });
    });
  },

  // ==========================================
  // 04. DEMO VIDEO MODAL HANDLER
  // ==========================================
  handleDemoVideoButton() {
    const demoBtn = document.querySelector(this.SELECTORS.DEMO_VIDEO_BTN);
    if (!demoBtn) return;

    demoBtn.addEventListener('click', () => {
      this.showVideoModal();
    });
  },

  /**
   * Hiển thị video modal
   */
  showVideoModal() {
    // Kiểm tra xem UI module có sẵn không
    if (typeof UIModule !== 'undefined' && UIModule.showModal) {
      const videoModal = UIModule.showModal({
        title: 'Video Demo - English 12 Smart',
        content: `
          <div class="aspect-video w-full bg-black rounded-lg overflow-hidden">
            <iframe 
              class="w-full h-full" 
              src="https://www.youtube.com/embed/dQw4w9WgXcQ" 
              title="English 12 Smart Demo" 
              frameborder="0" 
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
              allowfullscreen>
            </iframe>
          </div>
        `,
        size: 'large'
      });
      
      this.STATE.videoModalOpen = true;
      console.log('🎬 Video modal opened');
    } else {
      // Fallback: Đơn giản notify user
      alert('Xem video demo tại: https://youtube.com\n\nVideo này sẽ được integrate sau.');
      console.warn('⚠️ UIModule chưa sẵn sàng. Fallback alert.');
    }
  },

  // ==========================================
  // 05. AUTH REDIRECT HANDLERS
  // ==========================================
  handleAuthButtons() {
    // Desktop & Mobile Login Links
    const loginLinks = document.querySelectorAll(
      'a[href="/auth/login"]'
    );
    
    loginLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        console.log('🔐 Redirecting to login page');
        this.trackButtonClick('login_button');
      });
    });

    // Desktop & Mobile Register Links
    const registerLinks = document.querySelectorAll(
      'a[href="/auth/register"]'
    );
    
    registerLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        console.log('📝 Redirecting to register page');
        this.trackButtonClick('register_button');
      });
    });
  },

  // ==========================================
  // 06. COUNTER ANIMATION HANDLER
  // ==========================================
  handleCounterAnimation() {
    const counters = document.querySelectorAll(this.SELECTORS.COUNTERS);
    if (counters.length === 0) return;

    // Intersection Observer để trigger animation khi section vào view
    const observerOptions = {
      threshold: 0.5,
      rootMargin: '0px'
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !entry.target.hasAttribute('data-animated')) {
          // Đánh dấu đã animate để không repeat
          entry.target.setAttribute('data-animated', 'true');
          
          // Lấy target value
          const target = parseInt(entry.target.getAttribute('data-target'), 10);
          
          // Animate counter
          this.animateCounter(entry.target, target);
        }
      });
    }, observerOptions);

    counters.forEach(counter => observer.observe(counter));
  },

  /**
   * Animate counter từ 0 đến target value
   * @param {Element} element - Counter element
   * @param {number} target - Target value
   * @param {number} duration - Animation duration (ms)
   */
  animateCounter(element, target, duration = 2000) {
    const increment = target / (duration / 16); // 60fps
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if (current >= target) {
        current = target;
        clearInterval(timer);
      }
      
      // Format number với thousand separator
      const formatted = Math.floor(current).toLocaleString('vi-VN');
      element.textContent = formatted;
    }, 16); // ~60fps
  },

  // ==========================================
  // 07. BUTTON HOVER EFFECTS
  // ==========================================
  handleButtonHoverEffects() {
    // Tìm tất cả buttons
    const buttons = document.querySelectorAll(
      'button, a.bg-brand-500, a.shadow-lg, a.inline-block'
    );

    buttons.forEach(btn => {
      // Add ripple effect on click
      btn.addEventListener('click', function(e) {
        if (this.classList.contains('md:hidden') || 
            this.getAttribute('id') === 'mobile-menu-toggle') return;

        // Ripple effect (optional)
        const rect = this.getBoundingClientRect();
        const ripple = document.createElement('span');
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;

        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        ripple.className = 'ripple';

        // Cleanup ripple
        setTimeout(() => ripple.remove(), 600);
      });
    });
  },

  // ==========================================
  // 08. NAVIGATION ACTIVE STATE
  // ==========================================
  handleNavActiveState() {
    // Lắng nghe scroll event
    window.addEventListener('scroll', () => {
      this.updateActiveNavLink();
    });

    // Initial check
    this.updateActiveNavLink();
  },

  updateActiveNavLink() {
    const navLinks = document.querySelectorAll('nav a[href^="#"]');
    const sections = [
      { id: 'hero', offset: 0 },
      { id: 'features', offset: document.querySelector('#features')?.offsetTop || 0 },
      { id: 'footer', offset: document.querySelector('#footer')?.offsetTop || 0 }
    ];

    const scrollPosition = window.scrollY + 100; // Offset navbar height

    navLinks.forEach(link => {
      link.classList.remove('text-brand-600', 'font-semibold');
      link.classList.add('text-slate-500');
    });

    // Tìm section hiện tại
    sections.forEach((section, index) => {
      const nextSection = sections[index + 1];
      const isCurrentSection = nextSection 
        ? scrollPosition >= section.offset && scrollPosition < nextSection.offset
        : scrollPosition >= section.offset;

      if (isCurrentSection) {
        const selector = section.id === 'hero' 
          ? 'nav a[href="#"]'
          : `nav a[href="#${section.id}"]`;
        
        const activeLink = document.querySelector(selector);
        if (activeLink) {
          activeLink.classList.remove('text-slate-500');
          activeLink.classList.add('text-brand-600', 'font-semibold');
        }
      }
    });
  },

  // ==========================================
  // 09. ANALYTICS TRACKING
  // ==========================================
  trackButtonClick(buttonName) {
    // Nếu có Google Analytics hoặc custom analytics
    if (typeof gtag !== 'undefined') {
      gtag('event', 'button_click', {
        'button_name': buttonName,
        'page_path': window.location.pathname,
        'timestamp': new Date().toISOString()
      });
    }

    // Custom tracking
    console.log(`📊 Button tracked: ${buttonName}`);
  },

  // ==========================================
  // 10. INITIALIZATION
  // ==========================================
  init() {
    console.log('🚀 Initializing Button Handlers Module...');

    // Wait for DOM ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => {
        this.setupAllHandlers();
      });
    } else {
      this.setupAllHandlers();
    }
  },

  setupAllHandlers() {
    try {
      // Thiết lập tất cả handlers
      this.handleMobileMenuToggle();
      this.handleMobileMenuLinks();
      this.handleSmoothScroll();
      this.handleDemoVideoButton();
      this.handleAuthButtons();
      this.handleCounterAnimation();
      this.handleButtonHoverEffects();
      this.handleNavActiveState();

      console.log('✅ All button handlers initialized successfully!');
    } catch (error) {
      console.error('❌ Error initializing button handlers:', error);
    }
  }
};

// ============================================
// AUTO-INITIALIZE KHI MODULE LOAD
// ============================================
ButtonHandlers.init();
