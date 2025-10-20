import React, { useState } from 'react';

interface QueryFlowViewProps {
  stompClient: any;
}

interface QueryResult {
  queryId: string;
  sqlQuery: string;
  executionTimeMs: number;
  rowsReturned: number;
  status: string;
  results: Array<{ values: string[] }>;
}

const QueryFlowView: React.FC<QueryFlowViewProps> = ({ stompClient }) => {
  const [query, setQuery] = useState('SELECT name, age FROM users WHERE age > 30');
  const [isExecuting, setIsExecuting] = useState(false);
  const [result, setResult] = useState<QueryResult | null>(null);
  const [executionSteps, setExecutionSteps] = useState<string[]>([]);

  const sampleQueries = [
    'SELECT name, age FROM users WHERE age > 30',
    'SELECT COUNT(*) FROM users',
    'SELECT u.name, o.order_id FROM users u JOIN orders o ON u.user_id = o.user_id',
    'SELECT * FROM products WHERE category = \'Electronics\''
  ];

  const executeQuery = async () => {
    if (!query.trim()) return;
    
    setIsExecuting(true);
    setResult(null);
    setExecutionSteps([]);
    
    // Simulate execution steps
    const steps = [
      'Parsing SQL query...',
      'Creating execution plan...',
      'Distributing to workers...',
      'Executing on workers...',
      'Aggregating results...',
      'Returning results...'
    ];
    
    for (let i = 0; i < steps.length; i++) {
      setExecutionSteps(prev => [...prev, steps[i]]);
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    try {
      const response = await fetch('http://localhost:8080/api/query', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ query }),
      });
      
      const data = await response.json();
      
      if (data.success) {
        setResult(data.result);
      } else {
        console.error('Query execution failed:', data.message);
      }
    } catch (error) {
      console.error('Error executing query:', error);
    } finally {
      setIsExecuting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Query Input */}
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">Query Execution</h2>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              SQL Query
            </label>
            <textarea
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              rows={3}
              placeholder="Enter your SQL query here..."
            />
          </div>
          
          <div className="flex space-x-2">
            <button
              onClick={executeQuery}
              disabled={isExecuting || !query.trim()}
              className="btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isExecuting ? 'Executing...' : 'Execute Query'}
            </button>
            
            <button
              onClick={() => setQuery('')}
              className="btn btn-secondary"
            >
              Clear
            </button>
          </div>
          
          {/* Sample Queries */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Sample Queries
            </label>
            <div className="flex flex-wrap gap-2">
              {sampleQueries.map((sampleQuery, index) => (
                <button
                  key={index}
                  onClick={() => setQuery(sampleQuery)}
                  className="btn btn-secondary text-sm"
                >
                  {sampleQuery}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
      
      {/* Execution Flow */}
      {(isExecuting || executionSteps.length > 0) && (
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Execution Flow</h3>
          <div className="space-y-2">
            {executionSteps.map((step, index) => (
              <div key={index} className="flex items-center space-x-3">
                <div className="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center text-white text-xs">
                  ✓
                </div>
                <span className="text-gray-700">{step}</span>
              </div>
            ))}
            {isExecuting && executionSteps.length < 6 && (
              <div className="flex items-center space-x-3">
                <div className="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center">
                  <div className="w-3 h-3 bg-white rounded-full animate-pulse"></div>
                </div>
                <span className="text-gray-700">Processing...</span>
              </div>
            )}
          </div>
        </div>
      )}
      
      {/* Query Results */}
      {result && (
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Query Results</h3>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div className="bg-gray-50 p-3 rounded-lg">
              <div className="text-sm text-gray-600">Query ID</div>
              <div className="font-medium">{result.queryId}</div>
            </div>
            <div className="bg-gray-50 p-3 rounded-lg">
              <div className="text-sm text-gray-600">Execution Time</div>
              <div className="font-medium">{result.executionTimeMs}ms</div>
            </div>
            <div className="bg-gray-50 p-3 rounded-lg">
              <div className="text-sm text-gray-600">Rows Returned</div>
              <div className="font-medium">{result.rowsReturned}</div>
            </div>
          </div>
          
          {result.results && result.results.length > 0 && (
            <div className="overflow-x-auto">
              <table className="min-w-full bg-white border border-gray-200 rounded-lg">
                <thead className="bg-gray-50">
                  <tr>
                    {result.results[0].values.map((_, index) => (
                      <th key={index} className="px-4 py-2 text-left text-sm font-medium text-gray-700 border-b">
                        Column {index + 1}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {result.results.map((row, rowIndex) => (
                    <tr key={rowIndex} className="hover:bg-gray-50">
                      {row.values.map((value, colIndex) => (
                        <td key={colIndex} className="px-4 py-2 text-sm text-gray-900 border-b">
                          {value}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default QueryFlowView;
