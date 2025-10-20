import React from 'react';

interface ArchitectureViewProps {
  systemStatus: any;
}

const ArchitectureView: React.FC<ArchitectureViewProps> = ({ systemStatus }) => {
  const components = systemStatus?.components || {};
  
  const getStatusColor = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'healthy':
        return 'text-green-600 bg-green-100';
      case 'unhealthy':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-yellow-600 bg-yellow-100';
    }
  };

  return (
    <div className="space-y-6">
      <div className="card">
        <h2 className="text-xl font-semibold mb-4">System Architecture</h2>
        
        {/* Architecture Diagram */}
        <div className="relative bg-gray-100 rounded-lg p-8 mb-6">
          <div className="flex justify-center items-center space-x-8">
            {/* Coordinator */}
            <div className="text-center">
              <div className="w-24 h-24 bg-blue-500 rounded-lg flex items-center justify-center text-white font-bold text-sm mb-2">
                Coordinator
              </div>
              <div className={`status-indicator ${getStatusColor(components.coordinator?.status)}`}>
                {components.coordinator?.status || 'Unknown'}
              </div>
            </div>
            
            {/* Arrow */}
            <div className="text-2xl text-gray-400">→</div>
            
            {/* Workers */}
            <div className="flex space-x-4">
              {['worker1', 'worker2', 'worker3'].map((workerId) => (
                <div key={workerId} className="text-center">
                  <div className="w-20 h-20 bg-green-500 rounded-lg flex items-center justify-center text-white font-bold text-xs mb-2">
                    {workerId}
                  </div>
                  <div className={`status-indicator ${getStatusColor(components[workerId]?.status)}`}>
                    {components[workerId]?.status || 'Unknown'}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
        
        {/* Component Details */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {Object.entries(components).map(([id, component]: [string, any]) => (
            <div key={id} className="card">
              <h3 className="font-semibold text-lg mb-2">{id}</h3>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-600">Status:</span>
                  <span className={`status-indicator ${getStatusColor(component.status)}`}>
                    {component.status}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">CPU:</span>
                  <span className="font-medium">{component.cpuUsage?.toFixed(1)}%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Memory:</span>
                  <span className="font-medium">{component.memoryUsage?.toFixed(1)}%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Connections:</span>
                  <span className="font-medium">{component.activeConnections}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
      
      {/* System Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Total Queries</h3>
          <div className="text-3xl font-bold text-blue-600">
            {systemStatus?.totalQueries || 0}
          </div>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Active Queries</h3>
          <div className="text-3xl font-bold text-green-600">
            {systemStatus?.activeQueries || 0}
          </div>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">System Uptime</h3>
          <div className="text-3xl font-bold text-purple-600">
            {systemStatus?.systemUptime ? `${Math.floor(systemStatus.systemUptime / 60)}m` : '0m'}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ArchitectureView;
