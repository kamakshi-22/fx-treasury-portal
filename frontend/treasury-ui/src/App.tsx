import React, { useEffect, useState } from 'react';
import type { Trade } from './types';
import { fetchInitialTrades } from './services/api';
import { TradeBlotter } from './components/trader/TradeBlotter';
import { DealCaptureModal } from './components/trader/DealCaptureModal';
import { PlusCircle, Layers, ShieldCheck } from 'lucide-react';
import { CONTROLLER_TOKEN } from './utils/tokens';
import axios from "axios";

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'trader' | 'controller'>('trader');
  const [trades, setTrades] = useState<Trade[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  // State to hold netting summary results from settlement-service
  const [nettingData, setNettingData] = useState<any[] | null>(null);

  // Load trades from trade-capture-service on startup
  useEffect(() => {
    fetchInitialTrades()
        .then(setTrades)
        .catch((err) => console.error('Error loading initial blotter', err));
  }, []);

  const handleTradeCreated = (newTrade: Trade) => {
    setTrades((prev) => [newTrade, ...prev]);
  };

  return (
      <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
        {/* Header bar */}
        <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur px-6 py-4 flex justify-between items-center">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-sky-500/10 border border-sky-500/20 rounded-lg text-sky-400 font-bold">
              FX
            </div>
            <div>
              <h1 className="text-lg font-bold">Treasury & Valuation Portal</h1>
              <p className="text-xs text-slate-400">Enterprise Real-Time Dealing & Netting Desk</p>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <div className="bg-slate-800 p-1 rounded-lg flex space-x-1 border border-slate-700">
              <button
                  onClick={() => setActiveTab('trader')}
                  className={`flex items-center space-x-2 px-3 py-1.5 rounded text-xs font-semibold ${
                      activeTab === 'trader' ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white'
                  }`}
              >
                <Layers size={14} />
                <span>Trader Blotter</span>
              </button>
              <button
                  onClick={() => setActiveTab('controller')}
                  className={`flex items-center space-x-2 px-3 py-1.5 rounded text-xs font-semibold ${
                      activeTab === 'controller' ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white'
                  }`}
              >
                <ShieldCheck size={14} />
                <span>Treasury Controller</span>
              </button>
            </div>

            <button
                onClick={() => setIsModalOpen(true)}
                className="flex items-center space-x-2 bg-emerald-600 hover:bg-emerald-500 text-white px-3 py-1.5 rounded text-xs font-semibold transition"
            >
              <PlusCircle size={14} />
              <span>Book Deal</span>
            </button>
          </div>
        </header>

        {/* Main trading desk content */}
        <main className="flex-1 p-6">
          {activeTab === 'trader' ? (
              <TradeBlotter initialTrades={trades} />
          ) : (
              <div className="bg-slate-900 border border-slate-800 rounded-lg p-6">
                <div className="flex justify-between items-center mb-6">
                  <div>
                    <h3 className="text-xl font-bold text-slate-100">Bilateral Netting & Settlement Desk</h3>
                    <p className="text-sm text-slate-400">Aggregate mutual currency exposure and generate SWIFT MT101 records</p>
                  </div>
                  <button
                      onClick={async () => {
                        try {
                          const res = await axios.post(
                              'http://localhost:8080/api/v1/settlement/netting-run',
                              {},
                              {
                                headers: { Authorization: `Bearer ${CONTROLLER_TOKEN}` }
                              }
                          );
                          setNettingData(res.data);
                        } catch (err) {
                          console.error("Netting run failed:", err);
                        }
                      }}
                      className="bg-sky-600 hover:bg-sky-500 text-white font-semibold text-xs px-4 py-2 rounded transition shadow-lg"
                  >
                    Run Bilateral Netting
                  </button>
                </div>

                {nettingData && (
                    <div className="space-y-4">
                      {nettingData.map((item: any) => (
                          <div key={item.currencyPair} className="bg-slate-800/60 border border-slate-700 p-4 rounded-lg flex justify-between items-center">
                            <div>
                              <span className="text-sm font-bold text-slate-200">{item.currencyPair}</span>
                              <div className="text-xs text-slate-400 mt-1">
                                Trades Included: {item.includedTradeRefs.join(', ')}
                              </div>
                            </div>
                            <div className="text-right flex items-center space-x-4">
                              <div>
                                <span className={`text-xs font-bold px-2 py-1 rounded ${item.settlementAction === 'RECEIVE' ? 'bg-emerald-950 text-emerald-400' : 'bg-rose-950 text-rose-400'}`}>
                                  {item.settlementAction}
                                </span>
                                <span className="text-sm font-mono font-bold text-white ml-2">
                                  {item.netAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                                </span>
                              </div>
                              <button
                                  onClick={async () => {
                                    try {
                                      const res = await axios.post(
                                          'http://localhost:8080/api/v1/settlement/export-mt101',
                                          item,
                                          {
                                            responseType: 'blob',
                                            headers: { Authorization: `Bearer ${CONTROLLER_TOKEN}` }
                                          }
                                      );
                                      const url = window.URL.createObjectURL(new Blob([res.data]));
                                      const link = document.createElement('a');
                                      link.href = url;
                                      link.setAttribute('download', `MT101_${item.currencyPair}.txt`);
                                      document.body.appendChild(link);
                                      link.click();
                                      window.URL.revokeObjectURL(url);
                                    } catch (err) {
                                      console.error("Export MT101 failed:", err);
                                    }
                                  }}
                                  className="bg-slate-700 hover:bg-slate-600 text-xs text-white px-3 py-1.5 rounded"
                              >
                                Download MT101
                              </button>
                            </div>
                          </div>
                      ))}
                    </div>
                )}
              </div>
          )}
        </main>

        <DealCaptureModal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            onTradeCreated={handleTradeCreated}
        />
      </div>
  );
};

export default App;