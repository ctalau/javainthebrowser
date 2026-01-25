import { useState, useEffect, useRef } from 'react'
import CodeMirror from '@uiw/react-codemirror'
import { java } from '@codemirror/lang-java'
import { Button } from '@/components/ui/button'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Play, Github, Loader2 } from 'lucide-react'
import './App.css'

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
  const [javacReady, setJavacReady] = useState(false)
  const javacRef = useRef<JavacAPI | null>(null)

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
    const loadJavacModule = async () => {
      try {
        // @ts-ignore - Dynamic import of external module
        const module = await import('/javac-api.js')
        const javac = await module.loadJavac({ scriptUrl: "javac/javac.nocache.js" })

        javacRef.current = javac
        ;(window as any).jibJavac = javac
        setJavacReady(true)
      } catch (error) {
        const errorMsg = String(error)
        console.warn('Failed to load javac API', error)

        if (errorMsg.includes('Failed to load javac script')) {
          setOutput('Java compiler not available.\n\nThe GWT-compiled Java compiler files are missing.\n\nTo build them locally, run:\n  mvn clean package')
        } else if (errorMsg.includes('Timed out')) {
          setOutput('Java compiler initialization timed out.\n\nTry refreshing the page.')
        }
      }
    }

    loadJavacModule()
  }, [])

  // Capture GWT module output
  useEffect(() => {
    const logDiv = document.getElementById('log-div')
    if (!logDiv) return

    const observer = new MutationObserver(() => {
      const textarea = logDiv.querySelector('textarea')
      if (textarea) {
        setOutput((textarea as HTMLTextAreaElement).value)
      }
    })

    observer.observe(logDiv, { childList: true, subtree: true, characterData: true })

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
      const btnDiv = document.getElementById('btn-div')
      const gwtButton = btnDiv?.querySelector('button')

      if (gwtButton) {
        ;(gwtButton as HTMLButtonElement).click()
        setTimeout(() => {
          setIsRunning(false)
        }, 1000)
      } else {
        setOutput('Error: GWT module not ready. Please wait and try again.')
        setIsRunning(false)
      }
    } catch (error) {
      setOutput(`Error: ${error}`)
      setIsRunning(false)
    }
  }

  return (
    <div className="min-h-screen bg-zinc-50 flex flex-col">
      {/* Hidden divs for GWT module */}
      <div id="log-div" style={{ display: 'none' }}></div>
      <div id="btn-div" style={{ display: 'none' }}></div>

      {/* Header */}
      <header className="border-b bg-white px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold text-zinc-900">Java in the Browser</h1>
            <p className="text-sm text-zinc-500">Compile and run Java code directly in your browser</p>
          </div>
          <a
            href="https://github.com/ctalau/javainthebrowser"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-sm text-zinc-600 hover:text-zinc-900"
          >
            <Github className="h-5 w-5" />
            <span className="hidden sm:inline">GitHub</span>
          </a>
        </div>
      </header>

      {/* Main content */}
      <main className="flex-1 p-4 md:p-6">
        <div className="max-w-7xl mx-auto grid gap-4 md:gap-6 lg:grid-cols-2">
          {/* Editor */}
          <div className="flex flex-col gap-4">
            <Card className="flex-1 flex flex-col overflow-hidden">
              <CardHeader className="py-3 px-4 border-b bg-zinc-50">
                <CardTitle className="text-sm font-medium text-zinc-700">Source Code</CardTitle>
              </CardHeader>
              <CardContent className="flex-1 p-0 min-h-[400px]">
                <CodeMirror
                  value={code}
                  height="100%"
                  theme="light"
                  extensions={[java()]}
                  onChange={(value) => setCode(value)}
                  className="h-full text-sm"
                />
              </CardContent>
            </Card>

            <Button
              onClick={runCode}
              disabled={isRunning || !javacReady}
              size="lg"
              className="w-full"
            >
              {isRunning ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Compiling...
                </>
              ) : !javacReady ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Loading...
                </>
              ) : (
                <>
                  <Play className="h-4 w-4" />
                  Run Code
                </>
              )}
            </Button>
          </div>

          {/* Output */}
          <Card className="flex flex-col overflow-hidden">
            <CardHeader className="py-3 px-4 border-b bg-zinc-50">
              <CardTitle className="text-sm font-medium text-zinc-700">Output</CardTitle>
            </CardHeader>
            <CardContent className="flex-1 p-4 min-h-[300px] bg-zinc-900">
              <pre className="text-sm text-green-400 font-mono whitespace-pre-wrap">
                {output || '> Ready to compile Java code...'}
              </pre>
            </CardContent>
          </Card>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t bg-white px-6 py-3">
        <div className="max-w-7xl mx-auto flex items-center justify-between text-sm text-zinc-500">
          <span>Powered by GWT, CodeMirror, and React</span>
          <span className={javacReady ? 'text-green-600' : 'text-zinc-400'}>
            {javacReady ? 'Ready' : 'Loading...'}
          </span>
        </div>
      </footer>
    </div>
  )
}

export default App
