import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from 'stompjs';
import './App.css';
import ArchitectureView from './components/ArchitectureView';
import DemoView from './components/DemoView';
import PerformanceView from './components/PerformanceView';
import QueryFlowView from './components/QueryFlowView';

interface SystemStatus {
  components: Record<string, ComponentStatus>;
  totalQueries: number;
  activeQueries: number;
  systemUptime: number;
  lastUpdated: number;
}

interface ComponentStatus {
  id: string;
  status: string;
  cpuUsage: number;
  memoryUsage: number;
  activeConnections: number;
  lastHeartbeat: number;
}

interface PerformanceMetrics {
  totalQueries: number;
  averageLatencyMs: number;
  queriesPerSecond: number;
  errorRate: number;
  workerUtilization: Record<string, WorkerMetrics>;
}

interface WorkerMetrics {
  cpuUsage: number;
  memoryUsage: number;
  activeConnections: number;
}

type ViewType = 'architecture' | 'query-flow' | 'performance' | 'demo';

function App() {
  const [currentView, setCurrentView] = useState<ViewType>('architecture');
  const [systemStatus, setSystemStatus] = useState<SystemStatus | null>(null);
  const [performanceMetrics, setPerformanceMetrics] = useState<PerformanceMetrics | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [stompClient, setStompClient] = useState<any>(null);

  useEffect(() => {
    // Initialize WebSocket connection
    const socket = new SockJS('http://localhost:8080/ws');
    const stomp = Stomp.over(socket);
    
    stomp.connect({}, () => {
      console.log('Connected to WebSocket');
      setIsConnected(true);
      
      // Subscribe to system status updates
      stomp.subscribe('/topic/system-status', (message) => {
        const data = JSON.parse(message.body);
        setSystemStatus(data.data);
      });
      
      // Subscribe to performance metrics updates
      stomp.subscribe('/topic/metrics', (message) => {
        const data = JSON.parse(message.body);
        setPerformanceMetrics(data.data);
      });
      
      // Subscribe to query execution updates
      stomp.subscribe('/topic/query-execution', (message) => {
        const data = JSON.parse(message.body);
        console.log('Query execution update:', data);
      });
    });
    
    stomp.onStompError = (error) => {
      console.error('STOMP error:', error);
      setIsConnected(false);
    };
    
    setStompClient(stomp);
    
    return () => {
      if (stomp.connected) {
        stomp.disconnect();
      }
    };
  }, []);

  const renderCurrentView = () => {
    switch (currentView) {
      case 'architecture':
        return <ArchitectureView systemStatus={systemStatus} />;
      case 'query-flow':
        return <QueryFlowView stompClient={stompClient} />;
      case 'performance':
        return <PerformanceView performanceMetrics={performanceMetrics} />;
      case 'demo':
        return <DemoView stompClient={stompClient} />;
      default:
        return <ArchitectureView systemStatus={systemStatus} />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">
                🚀 Distributed SQL Query Engine
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <div className={`w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`}></div>
                <span className="text-sm text-gray-600">
                  {isConnected ? 'Connected' : 'Disconnected'}
                </span>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex space-x-8">
            {[
              { id: 'architecture', label: 'Architecture' },
              { id: 'query-flow', label: 'Query Flow' },
              { id: 'performance', label: 'Performance' },
              { id: 'demo', label: 'Demo' }
            ].map((view) => (
              <button
                key={view.id}
                onClick={() => setCurrentView(view.id as ViewType)}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  currentView === view.id
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                {view.label}
              </button>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {renderCurrentView()}
      </main>
    </div>
  );
}

export default App;