import React, { useState } from 'react';

interface DemoViewProps {
  stompClient: any;
}

const DemoView: React.FC<DemoViewProps> = ({ stompClient }) => {
  const [isDemoRunning, setIsDemoRunning] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [demoResults, setDemoResults] = useState<any[]>([]);

  const demoSteps = [
    {
      title: 'System Overview',
      description: 'Show the distributed architecture with coordinator and workers',
      duration: 3000
    },
    {
      title: 'Query Execution',
      description: 'Execute a sample query and show the distributed processing',
      duration: 5000
    },
    {
      title: 'Performance Metrics',
      description: 'Display real-time performance metrics and worker utilization',
      duration: 4000
    },
    {
      title: 'Fault Tolerance',
      description: 'Demonstrate system resilience and recovery mechanisms',
      duration: 3000
    }
  ];

  const sampleQueries = [
    {
      query: 'SELECT name, age FROM users WHERE age > 30',
      description: 'Simple filtered query across sharded data'
    },
    {
      query: 'SELECT COUNT(*) FROM users',
      description: 'Aggregation query with distributed counting'
    },
    {
      query: 'SELECT u.name, o.order_id FROM users u JOIN orders o ON u.user_id = o.user_id',
      description: 'Complex JOIN query across multiple tables'
    }
  ];

  const runDemo = async () => {
    setIsDemoRunning(true);
    setCurrentStep(0);
    setDemoResults([]);

    for (let i = 0; i < demoSteps.length; i++) {
      setCurrentStep(i);
      
      // Execute sample queries during demo
      if (i === 1) {
        for (const sampleQuery of sampleQueries) {
          try {
            const response = await fetch('http://localhost:8080/api/query', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({ query: sampleQuery.query }),
            });
            
            const data = await response.json();
            
            if (data.success) {
              setDemoResults(prev => [...prev, {
                query: sampleQuery.query,
                description: sampleQuery.description,
                result: data.result
              }]);
            }
          } catch (error) {
            console.error('Error executing demo query:', error);
          }
          
          await new Promise(resolve => setTimeout(resolve, 1000));
        }
      }
      
      await new Promise(resolve => setTimeout(resolve, demoSteps[i].duration));
    }
    
    setIsDemoRunning(false);
  };

  const stopDemo = () => {
    setIsDemoRunning(false);
    setCurrentStep(0);
  };

  return (
    <div className="space-y-6">
      {/* Demo Controls */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">Interactive Demo</h2>
        
        <div className="flex space-x-4 mb-6">
          <button
            onClick={runDemo}
            disabled={isDemoRunning}
            className="btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isDemoRunning ? 'Running Demo...' : 'Start Demo'}
          </button>
          
          <button
            onClick={stopDemo}
            disabled={!isDemoRunning}
            className="btn btn-danger disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Stop Demo
          </button>
        </div>
        
        {/* Demo Progress */}
        {isDemoRunning && (
          <div className="space-y-4">
            <div className="bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${((currentStep + 1) / demoSteps.length) * 100}%` }}
              ></div>
            </div>
            
            <div className="text-center">
              <h3 className="text-lg font-semibold">
                {demoSteps[currentStep]?.title}
              </h3>
              <p className="text-gray-600">
                {demoSteps[currentStep]?.description}
              </p>
            </div>
          </div>
        )}
      </div>
      
      {/* Demo Results */}
      {demoResults.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Demo Query Results</h3>
          
          <div className="space-y-4">
            {demoResults.map((result, index) => (
              <div key={index} className="bg-gray-50 p-4 rounded-lg">
                <h4 className="font-medium text-gray-800 mb-2">
                  Query {index + 1}: {result.description}
                </h4>
                <div className="bg-white p-3 rounded border mb-2">
                  <code className="text-sm text-gray-700">{result.query}</code>
                </div>
                
                {result.result && (
                  <div className="grid grid-cols-3 gap-4 text-sm">
                    <div>
                      <span className="text-gray-600">Execution Time:</span>
                      <span className="font-medium ml-1">{result.result.executionTimeMs}ms</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Rows Returned:</span>
                      <span className="font-medium ml-1">{result.result.rowsReturned}</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Status:</span>
                      <span className="font-medium ml-1">{result.result.status}</span>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
      
      {/* Educational Content */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Distributed Systems Concepts</h3>
          <div className="space-y-3">
            <div>
              <h4 className="font-medium text-gray-800">Microservices Architecture</h4>
              <p className="text-sm text-gray-600">
                The system uses a coordinator-worker pattern where the coordinator manages query planning and execution across multiple worker nodes.
              </p>
            </div>
            <div>
              <h4 className="font-medium text-gray-800">Data Sharding</h4>
              <p className="text-sm text-gray-600">
                Data is distributed across workers using hash-based and range-based sharding strategies for optimal query performance.
              </p>
            </div>
            <div>
              <h4 className="font-medium text-gray-800">Fault Tolerance</h4>
              <p className="text-sm text-gray-600">
                The system includes health monitoring, automatic failure detection, and query retry mechanisms for reliability.
              </p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Technology Stack</h3>
          <div className="space-y-3">
            <div>
              <h4 className="font-medium text-gray-800">Backend</h4>
              <p className="text-sm text-gray-600">
                Java 17, Spring Boot, gRPC, Protocol Buffers, PostgreSQL, HikariCP connection pooling
              </p>
            </div>
            <div>
              <h4 className="font-medium text-gray-800">Frontend</h4>
              <p className="text-sm text-gray-600">
                React 18, TypeScript, TailwindCSS, Recharts, WebSocket for real-time updates
              </p>
            </div>
            <div>
              <h4 className="font-medium text-gray-800">Communication</h4>
              <p className="text-sm text-gray-600">
                gRPC for high-performance inter-service communication, WebSocket for real-time visualization
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DemoView;
