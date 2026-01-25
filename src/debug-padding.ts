/**
 * Debug Padding Script
 * Analyzes the DOM to find sources of white padding and spacing issues
 */

interface ElementInfo {
  selector: string;
  element: HTMLElement;
  width: number;
  height: number;
  offsetLeft: number;
  offsetTop: number;
  scrollWidth: number;
  scrollHeight: number;
  clientWidth: number;
  clientHeight: number;
  computedStyle: {
    margin: string;
    padding: string;
    border: string;
    width: string;
    height: string;
    position: string;
    display: string;
    overflow: string;
    boxSizing: string;
  };
}

export class PaddingDebugger {
  private debugPanel: HTMLDivElement | null = null;
  private isActive = false;

  constructor() {
    // Auto-run analysis when instantiated
    this.init();
  }

  private init() {
    console.log('%c🔍 Padding Debugger Initialized', 'color: #0ff; font-weight: bold; font-size: 14px;');
    console.log('%cPress Ctrl+Shift+D to toggle debug mode', 'color: #0f0;');

    // Add keyboard shortcut
    document.addEventListener('keydown', (e) => {
      if (e.ctrlKey && e.shiftKey && e.key === 'D') {
        this.toggle();
      }
    });

    // Auto-analyze on load
    setTimeout(() => this.analyze(), 1000);
  }

  public toggle() {
    this.isActive = !this.isActive;

    if (this.isActive) {
      document.body.classList.add('debug-padding-mode');
      this.showDebugPanel();
      this.analyze();
    } else {
      document.body.classList.remove('debug-padding-mode');
      this.hideDebugPanel();
    }
  }

  public analyze() {
    console.group('%c📊 Layout Analysis Report', 'color: #ff0; font-weight: bold; font-size: 16px;');

    const elements = [
      { selector: 'html', element: document.documentElement },
      { selector: 'body', element: document.body },
      { selector: '#root', element: document.getElementById('root') },
      { selector: '.app', element: document.querySelector('.app') },
      { selector: '.header', element: document.querySelector('.header') },
      { selector: '.main-content', element: document.querySelector('.main-content') },
      { selector: '.footer', element: document.querySelector('.footer') },
    ];

    const results: ElementInfo[] = [];

    elements.forEach(({ selector, element }) => {
      if (!element) {
        console.warn(`Element ${selector} not found`);
        return;
      }

      const info = this.getElementInfo(selector, element as HTMLElement);
      results.push(info);
      this.logElementInfo(info);
    });

    // Check for overflow issues
    console.group('%c⚠️ Overflow Analysis', 'color: #f80; font-weight: bold;');
    this.checkOverflow(results);
    console.groupEnd();

    // Check viewport
    console.group('%c📱 Viewport Information', 'color: #0ff; font-weight: bold;');
    this.logViewportInfo();
    console.groupEnd();

    // Check for elements wider than viewport
    console.group('%c📏 Width Analysis', 'color: #f0f; font-weight: bold;');
    this.checkWidthIssues();
    console.groupEnd();

    console.groupEnd();

    // Update debug panel if active
    if (this.isActive) {
      this.updateDebugPanel(results);
    }

    return results;
  }

  private getElementInfo(selector: string, element: HTMLElement): ElementInfo {
    const computed = window.getComputedStyle(element);
    const rect = element.getBoundingClientRect();

    return {
      selector,
      element,
      width: rect.width,
      height: rect.height,
      offsetLeft: element.offsetLeft,
      offsetTop: element.offsetTop,
      scrollWidth: element.scrollWidth,
      scrollHeight: element.scrollHeight,
      clientWidth: element.clientWidth,
      clientHeight: element.clientHeight,
      computedStyle: {
        margin: computed.margin,
        padding: computed.padding,
        border: computed.border,
        width: computed.width,
        height: computed.height,
        position: computed.position,
        display: computed.display,
        overflow: computed.overflow,
        boxSizing: computed.boxSizing,
      },
    };
  }

  private logElementInfo(info: ElementInfo) {
    console.group(`%c${info.selector}`, 'color: #0f0; font-weight: bold;');

    console.log('📐 Dimensions:', {
      'Bounding Width': `${info.width.toFixed(2)}px`,
      'Bounding Height': `${info.height.toFixed(2)}px`,
      'Client Width': `${info.clientWidth}px`,
      'Client Height': `${info.clientHeight}px`,
      'Scroll Width': `${info.scrollWidth}px`,
      'Scroll Height': `${info.scrollHeight}px`,
      'Offset Left': `${info.offsetLeft}px`,
      'Offset Top': `${info.offsetTop}px`,
    });

    console.log('📦 Box Model:', {
      'Margin': info.computedStyle.margin,
      'Padding': info.computedStyle.padding,
      'Border': info.computedStyle.border,
      'Box Sizing': info.computedStyle.boxSizing,
    });

    console.log('🎨 Layout:', {
      'Width': info.computedStyle.width,
      'Height': info.computedStyle.height,
      'Position': info.computedStyle.position,
      'Display': info.computedStyle.display,
      'Overflow': info.computedStyle.overflow,
    });

    // Check for potential issues
    if (info.scrollWidth > info.clientWidth) {
      console.warn(`⚠️ Horizontal overflow detected! scrollWidth (${info.scrollWidth}) > clientWidth (${info.clientWidth})`);
    }

    if (info.scrollHeight > info.clientHeight) {
      console.warn(`⚠️ Vertical overflow detected! scrollHeight (${info.scrollHeight}) > clientHeight (${info.clientHeight})`);
    }

    console.groupEnd();
  }

  private checkOverflow(results: ElementInfo[]) {
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    results.forEach((info) => {
      if (info.scrollWidth > viewportWidth) {
        console.warn(`${info.selector}: scrollWidth (${info.scrollWidth}px) exceeds viewport width (${viewportWidth}px) by ${info.scrollWidth - viewportWidth}px`);
      }

      if (info.width > viewportWidth) {
        console.warn(`${info.selector}: width (${info.width}px) exceeds viewport width (${viewportWidth}px) by ${info.width - viewportWidth}px`);
      }
    });
  }

  private logViewportInfo() {
    console.log('📱 Viewport:', {
      'Window Inner Width': `${window.innerWidth}px`,
      'Window Inner Height': `${window.innerHeight}px`,
      'Window Outer Width': `${window.outerWidth}px`,
      'Window Outer Height': `${window.outerHeight}px`,
      'Screen Width': `${window.screen.width}px`,
      'Screen Height': `${window.screen.height}px`,
      'Device Pixel Ratio': window.devicePixelRatio,
    });

    const html = document.documentElement;
    console.log('📄 Document:', {
      'Document Width': `${html.scrollWidth}px`,
      'Document Height': `${html.scrollHeight}px`,
      'Client Width': `${html.clientWidth}px`,
      'Client Height': `${html.clientHeight}px`,
    });
  }

  private checkWidthIssues() {
    const viewportWidth = window.innerWidth;
    const allElements = document.querySelectorAll('*');
    const wideElements: { element: Element; width: number; overflow: number }[] = [];

    allElements.forEach((el) => {
      const rect = el.getBoundingClientRect();
      if (rect.width > viewportWidth) {
        wideElements.push({
          element: el,
          width: rect.width,
          overflow: rect.width - viewportWidth,
        });
      }
    });

    if (wideElements.length > 0) {
      console.warn(`Found ${wideElements.length} elements wider than viewport:`);
      wideElements
        .sort((a, b) => b.overflow - a.overflow)
        .slice(0, 10)
        .forEach(({ element, width, overflow }) => {
          const selector = this.getElementSelector(element);
          console.warn(`  ${selector}: ${width.toFixed(2)}px (${overflow.toFixed(2)}px overflow)`, element);
        });
    } else {
      console.log('✅ No elements exceed viewport width');
    }
  }

  private getElementSelector(element: Element): string {
    if (element.id) return `#${element.id}`;
    if (element.className && typeof element.className === 'string') {
      const classes = element.className.trim().split(/\s+/).join('.');
      return `.${classes}`;
    }
    return element.tagName.toLowerCase();
  }

  private showDebugPanel() {
    if (this.debugPanel) return;

    this.debugPanel = document.createElement('div');
    this.debugPanel.className = 'debug-box-model-panel';
    this.debugPanel.innerHTML = `
      <h3>🔍 Padding Debugger</h3>
      <div id="debug-panel-content">Analyzing...</div>
      <button onclick="window.paddingDebugger?.analyze()">🔄 Re-analyze</button>
      <button onclick="window.paddingDebugger?.toggle()">❌ Close</button>
    `;
    document.body.appendChild(this.debugPanel);
  }

  private hideDebugPanel() {
    if (this.debugPanel) {
      this.debugPanel.remove();
      this.debugPanel = null;
    }
  }

  private updateDebugPanel(results: ElementInfo[]) {
    if (!this.debugPanel) return;

    const content = document.getElementById('debug-panel-content');
    if (!content) return;

    let html = '';
    results.forEach((info) => {
      const hasOverflow = info.scrollWidth > info.clientWidth || info.scrollHeight > info.clientHeight;

      html += `
        <div class="element-info">
          <div class="element-name">${info.selector}</div>
          <div class="property">
            <span class="property-name">Width:</span>
            <span class="property-value">${info.width.toFixed(2)}px</span>
          </div>
          <div class="property">
            <span class="property-name">Scroll Width:</span>
            <span class="property-value">${info.scrollWidth}px</span>
          </div>
          <div class="property">
            <span class="property-name">Padding:</span>
            <span class="property-value">${info.computedStyle.padding}</span>
          </div>
          <div class="property">
            <span class="property-name">Margin:</span>
            <span class="property-value">${info.computedStyle.margin}</span>
          </div>
          ${hasOverflow ? '<div class="warning">⚠️ Overflow detected!</div>' : ''}
        </div>
      `;
    });

    content.innerHTML = html;
  }

  // Public API for manual checks
  public checkElement(selector: string) {
    const element = document.querySelector(selector);
    if (!element) {
      console.error(`Element ${selector} not found`);
      return;
    }
    const info = this.getElementInfo(selector, element as HTMLElement);
    this.logElementInfo(info);
    return info;
  }

  public findWidestElement() {
    const allElements = document.querySelectorAll('*');
    let widest: { element: Element; width: number } | null = null;

    allElements.forEach((el) => {
      const rect = el.getBoundingClientRect();
      if (!widest || rect.width > widest.width) {
        widest = { element: el, width: rect.width };
      }
    });

    if (widest) {
      console.log('Widest element:', this.getElementSelector(widest.element), `${widest.width}px`, widest.element);
      return widest;
    }
  }
}

// Auto-initialize and expose globally
if (typeof window !== 'undefined') {
  const debugger = new PaddingDebugger();
  (window as any).paddingDebugger = debugger;

  // Also expose helper functions
  (window as any).analyzeLayout = () => debugger.analyze();
  (window as any).checkElement = (selector: string) => debugger.checkElement(selector);
  (window as any).findWidestElement = () => debugger.findWidestElement();

  console.log('%c💡 Helper Functions Available:', 'color: #0ff; font-weight: bold;');
  console.log('%c  analyzeLayout() - Run full layout analysis', 'color: #0f0;');
  console.log('%c  checkElement(selector) - Analyze specific element', 'color: #0f0;');
  console.log('%c  findWidestElement() - Find widest element on page', 'color: #0f0;');
  console.log('%c  paddingDebugger.toggle() - Toggle visual debug mode', 'color: #0f0;');
  console.log('%c  Ctrl+Shift+D - Toggle debug mode', 'color: #0f0;');
}

export default PaddingDebugger;
