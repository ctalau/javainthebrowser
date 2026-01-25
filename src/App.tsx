import { useState, useEffect, useRef } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import CodeMirror from '@uiw/react-codemirror'
import { java } from '@codemirror/lang-java'
import './App.css'

// Debug log types
type LogType = 'info' | 'success' | 'error' | 'warning' | 'milestone'

interface DebugLogEntry {
  timestamp: string
  type: LogType
  message: string
}

// Javac API types
interface CompilationResult {
  success: boolean
  className?: string
  classFile?: Uint8Array
  errors?: string[]
}

interface JavacAPI {
  compile: (source: string, options?: { fileName?: string }) => CompilationResult
  getClassName: (source: string) => string
}

// Default Java code
const DEFAULT_CODE = `public class HelloWorld {
  public static void main(String[] args) {
    System.out.println("Hello, World!");
    System.out.println("Welcome to Java in the Browser!");
  }
}`

function App() {
  const [code, setCode] = useState(DEFAULT_CODE)
  const [output, setOutput] = useState('')
  const [isRunning, setIsRunning] = useState(false)
  const [debugLogs, setDebugLogs] = useState<DebugLogEntry[]>([])
  const [showDebugLogs, setShowDebugLogs] = useState(false)
  const [javacReady, setJavacReady] = useState(false)
  const [paddingDebugMode, setPaddingDebugMode] = useState(false)
  const [paddingDebugResults, setPaddingDebugResults] = useState<string>('')
  const debugLogRef = useRef<HTMLDivElement>(null)
  const javacRef = useRef<JavacAPI | null>(null)

  // Debug logging function
  const addDebugLog = (message: string, type: LogType = 'info') => {
    const now = new Date()
    const timestamp = now.toTimeString().split(' ')[0] + '.' + now.getMilliseconds().toString().padStart(3, '0')

    setDebugLogs(prev => [...prev, { timestamp, type, message }])

    // Auto-scroll to bottom
    setTimeout(() => {
      if (debugLogRef.current) {
        debugLogRef.current.scrollTop = debugLogRef.current.scrollHeight
      }
    }, 10)
  }

  // Expose editor to window for GWT module
  useEffect(() => {
    (window as any).editor = {
      getCode: () => code
    }
    return () => {
      delete (window as any).editor
    }
  }, [code])

  // Load javac API
  useEffect(() => {
    addDebugLog('Initializing Java in the Browser...', 'milestone')

    // Load the javac module - EXACT same approach as original Jib.html
    const loadJavacModule = async () => {
      try {
        addDebugLog('Starting javac module load', 'milestone')

        // Dynamic import of javac-api.js
        // @ts-ignore - Dynamic import of external module
        const module = await import('/javac-api.js')

        // Load javac with the EXACT same scriptUrl as original
        const javac = await module.loadJavac({ scriptUrl: "javac/javac.nocache.js" })

        javacRef.current = javac
        // Also store in window for compatibility
        ;(window as any).jibJavac = javac

        setJavacReady(true)
        addDebugLog('Javac module loaded successfully', 'success')
        addDebugLog('Ready to compile Java code', 'milestone')
      } catch (error) {
        const errorMsg = String(error)
        console.warn('Failed to load javac API', error)
        addDebugLog(`Failed to load javac API: ${errorMsg}`, 'error')

        if (errorMsg.includes('Failed to load javac script')) {
          addDebugLog('GWT files not found. Run "mvn clean package" to build them.', 'error')
          setOutput('⚠️ Java compiler not available\n\nThe GWT-compiled Java compiler files are missing.\n\nTo build them locally, run:\n  mvn clean package\n\nOr check the deployment logs if using Vercel.')
        } else if (errorMsg.includes('Timed out')) {
          addDebugLog('Javac module timed out. GWT files may be corrupted or incomplete.', 'error')
          setOutput('⚠️ Java compiler initialization timed out\n\nThe compiler took too long to load. Try refreshing the page.')
        }
      }
    }

    loadJavacModule()

    // Global error handlers
    const handleError = (event: ErrorEvent) => {
      addDebugLog(`Unhandled error: ${event.message}`, 'error')
    }

    const handleUnhandledRejection = (event: PromiseRejectionEvent) => {
      addDebugLog(`Unhandled promise rejection: ${event.reason}`, 'error')
    }

    window.addEventListener('error', handleError)
    window.addEventListener('unhandledrejection', handleUnhandledRejection)

    return () => {
      window.removeEventListener('error', handleError)
      window.removeEventListener('unhandledrejection', handleUnhandledRejection)
    }
  }, [])

  // Capture GWT module output
  useEffect(() => {
    // Set up mutation observer to watch the GWT log div
    const logDiv = document.getElementById('log-div')
    if (!logDiv) return

    const observer = new MutationObserver(() => {
      // Get the textarea that GWT creates
      const textarea = logDiv.querySelector('textarea')
      if (textarea) {
        setOutput((textarea as HTMLTextAreaElement).value)
      }
    })

    observer.observe(logDiv, { childList: true, subtree: true, characterData: true })

    // Also watch for value changes
    const checkOutput = setInterval(() => {
      const textarea = logDiv.querySelector('textarea')
      if (textarea) {
        const value = (textarea as HTMLTextAreaElement).value
        setOutput(value)
      }
    }, 100)

    return () => {
      observer.disconnect()
      clearInterval(checkOutput)
    }
  }, [])

  // Run Java code by clicking the GWT button
  const runCode = async () => {
    setIsRunning(true)

    try {
      // Find and click the GWT button
      const btnDiv = document.getElementById('btn-div')
      const gwtButton = btnDiv?.querySelector('button')

      if (gwtButton) {
        addDebugLog('Triggering GWT run button', 'info')
        ;(gwtButton as HTMLButtonElement).click()

        // Wait a bit for execution to complete
        setTimeout(() => {
          setIsRunning(false)
        }, 1000)
      } else {
        addDebugLog('GWT button not found - module may not be loaded yet', 'warning')
        setOutput('Error: GWT module not ready. Please wait and try again.')
        setIsRunning(false)
      }
    } catch (error) {
      addDebugLog(`Compilation error: ${error}`, 'error')
      setOutput(`Error: ${error}`)
      setIsRunning(false)
    }
  }

  // Toggle padding debug mode
  const togglePaddingDebug = () => {
    const newMode = !paddingDebugMode
    setPaddingDebugMode(newMode)

    if (newMode) {
      document.body.classList.add('debug-padding-mode')
      runPaddingAnalysis()
    } else {
      document.body.classList.remove('debug-padding-mode')
      setPaddingDebugResults('')
    }
  }

  // Run padding analysis
  const runPaddingAnalysis = () => {
    const elements = [
      { selector: 'html', element: document.documentElement },
      { selector: 'body', element: document.body },
      { selector: '#root', element: document.getElementById('root') },
      { selector: '.app', element: document.querySelector('.app') },
      { selector: '.header', element: document.querySelector('.header') },
      { selector: '.main-content', element: document.querySelector('.main-content') },
      { selector: '.footer', element: document.querySelector('.footer') },
    ]

    let results = '=== PADDING DEBUG ANALYSIS ===\n\n'
    results += `Viewport: ${window.innerWidth}px × ${window.innerHeight}px\n\n`

    elements.forEach(({ selector, element }) => {
      if (!element) {
        results += `❌ ${selector}: NOT FOUND\n\n`
        return
      }

      const el = element as HTMLElement
      const computed = window.getComputedStyle(el)
      const rect = el.getBoundingClientRect()

      results += `📦 ${selector}\n`
      results += `  Width: ${rect.width.toFixed(1)}px (scroll: ${el.scrollWidth}px)\n`
      results += `  Height: ${rect.height.toFixed(1)}px (scroll: ${el.scrollHeight}px)\n`
      results += `  Padding: ${computed.padding}\n`
      results += `  Margin: ${computed.margin}\n`
      results += `  Border: ${computed.borderWidth}\n`
      results += `  Box-sizing: ${computed.boxSizing}\n`

      if (el.scrollWidth > el.clientWidth) {
        results += `  ⚠️ HORIZONTAL OVERFLOW: ${el.scrollWidth - el.clientWidth}px\n`
      }
      if (el.scrollHeight > el.clientHeight) {
        results += `  ⚠️ VERTICAL OVERFLOW: ${el.scrollHeight - el.clientHeight}px\n`
      }
      if (rect.width > window.innerWidth) {
        results += `  ⚠️ WIDER THAN VIEWPORT: ${(rect.width - window.innerWidth).toFixed(1)}px\n`
      }

      results += '\n'
    })

    // Find widest element
    const allElements = document.querySelectorAll('*')
    let widest = { selector: '', width: 0, element: null as Element | null }

    allElements.forEach((el) => {
      const rect = el.getBoundingClientRect()
      if (rect.width > widest.width) {
        widest = {
          selector: el.id ? `#${el.id}` : el.className ? `.${el.className.split(' ')[0]}` : el.tagName.toLowerCase(),
          width: rect.width,
          element: el
        }
      }
    })

    results += `🔍 WIDEST ELEMENT:\n`
    results += `  ${widest.selector}: ${widest.width.toFixed(1)}px\n`
    if (widest.width > window.innerWidth) {
      results += `  ⚠️ Exceeds viewport by ${(widest.width - window.innerWidth).toFixed(1)}px\n`
    }

    setPaddingDebugResults(results)
  }

  return (
    <div className="app">
      {/* Hidden divs for GWT module to attach to */}
      <div id="log-div" style={{ display: 'none' }}></div>
      <div id="btn-div" style={{ display: 'none' }}></div>

      {/* Animated scan line effect */}
      <div className="scan-line" />

      {/* CRT noise overlay */}
      <div className="crt-overlay" />

      {/* Header */}
      <motion.header
        className="header"
        initial={{ opacity: 0, y: -50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
      >
        <div className="header-content">
          <motion.h1
            className="title glow"
            initial={{ scale: 0.9 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            JAVA.BROWSER
          </motion.h1>
          <motion.p
            className="subtitle"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.4 }}
          >
            Compile & Execute Java in Your Browser
          </motion.p>
        </div>

        <a
          href="https://github.com/ctalau/javainthebrowser"
          className="github-link"
          target="_blank"
          rel="noopener noreferrer"
        >
          <svg width="24" height="24" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/>
          </svg>
          <span>Fork on GitHub</span>
        </a>
      </motion.header>

      {/* Main content */}
      <main className="main-content">
        <div className="editor-section">
          <motion.div
            className="panel editor-panel"
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
          >
            <div className="panel-header">
              <span className="panel-title">SOURCE CODE</span>
              <span className="panel-indicator"></span>
            </div>
            <div className="editor-wrapper">
              <CodeMirror
                value={code}
                height="100%"
                theme="dark"
                extensions={[java()]}
                onChange={(value) => setCode(value)}
                className="code-editor"
              />
            </div>
          </motion.div>

          <motion.div
            className="controls"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <button
              onClick={runCode}
              disabled={isRunning || !javacReady}
              className="run-button"
            >
              {isRunning ? (
                <>
                  <span className="spinner"></span>
                  COMPILING...
                </>
              ) : !javacReady ? (
                'LOADING...'
              ) : (
                <>
                  <span className="play-icon">▶</span>
                  RUN CODE
                </>
              )}
            </button>
          </motion.div>
        </div>

        <div className="output-section">
          <motion.div
            className="panel output-panel"
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
          >
            <div className="panel-header">
              <span className="panel-title">OUTPUT CONSOLE</span>
              <span className="panel-indicator active"></span>
            </div>
            <div className="output-content">
              <pre>{output || '> Ready to compile Java code...'}</pre>
            </div>
          </motion.div>

          <AnimatePresence>
            {showDebugLogs && (
              <motion.div
                className="panel debug-panel"
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                transition={{ duration: 0.3 }}
              >
                <div className="panel-header">
                  <span className="panel-title">DEBUG LOGS</span>
                  <button
                    onClick={() => setDebugLogs([])}
                    className="clear-logs"
                  >
                    CLEAR
                  </button>
                </div>
                <div className="debug-content" ref={debugLogRef}>
                  {debugLogs.map((log, index) => (
                    <div key={index} className={`debug-entry debug-${log.type}`}>
                      <span className="debug-timestamp">[{log.timestamp}]</span>
                      <span className="debug-type">[{log.type.toUpperCase()}]</span>
                      <span className="debug-message">{log.message}</span>
                    </div>
                  ))}
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          <AnimatePresence>
            {paddingDebugMode && (
              <motion.div
                className="panel debug-panel"
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                transition={{ duration: 0.3 }}
              >
                <div className="panel-header">
                  <span className="panel-title">PADDING DEBUG</span>
                  <button
                    onClick={runPaddingAnalysis}
                    className="clear-logs"
                  >
                    REFRESH
                  </button>
                </div>
                <div className="debug-content">
                  <pre style={{ color: 'var(--color-neon-green)', fontSize: '0.75rem', whiteSpace: 'pre-wrap' }}>
                    {paddingDebugResults || 'Running analysis...'}
                  </pre>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </main>

      {/* Footer */}
      <motion.footer
        className="footer"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5, delay: 0.8 }}
      >
        <p>Powered by GWT • CodeMirror 6 • React</p>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <button
            onClick={() => setShowDebugLogs(!showDebugLogs)}
            className="footer-debug-toggle"
          >
            {showDebugLogs ? '◀ HIDE' : '▶'} DEBUG
          </button>
          <button
            onClick={togglePaddingDebug}
            className="footer-debug-toggle"
            style={{
              borderColor: paddingDebugMode ? 'var(--color-neon-cyan)' : 'var(--color-text-dim)',
              color: paddingDebugMode ? 'var(--color-neon-cyan)' : 'var(--color-text-dim)'
            }}
          >
            {paddingDebugMode ? '🔍 ON' : '🔍'} PADDING
          </button>
        </div>
        <p className="status">
          System Status: <span className={javacReady ? 'status-ready' : 'status-loading'}>
            {javacReady ? '● READY' : '○ LOADING...'}
          </span>
        </p>
      </motion.footer>
    </div>
  )
}

export default App
