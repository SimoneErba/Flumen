import './App.css'
import { DisplayGraph } from './components/graph'
import { useGraph } from './hooks/useGraph';

function App() {
  const { graphData, loading } = useGraph();
  return (
    loading ? <div>Loading...</div> : <div style={{
      width: '100vw',
      height: '100vh'
    }}>
      
      <DisplayGraph initialGraphData={graphData} />
    </div>
  );
}

export default App;