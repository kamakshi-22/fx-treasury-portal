import React, { useMemo, useRef, useCallback } from 'react';
import { AgGridReact } from 'ag-grid-react';
import {type ColDef, ModuleRegistry, AllCommunityModule } from 'ag-grid-community';
import type {Trade, ValuationEvent} from '../../types';
import { useValuationStream } from '../../hooks/useValuationStream';

// Register all standard community grid modules
ModuleRegistry.registerModules([AllCommunityModule]);

interface TradeBlotterProps {
    initialTrades: Trade[];
}

export const TradeBlotter: React.FC<TradeBlotterProps> = ({ initialTrades }) => {
    const gridRef = useRef<AgGridReact<Trade>>(null);

    // In-place AG Grid cell update on incoming SSE tick
    const handleValuationTick = useCallback((tick: ValuationEvent) => {
        if (!gridRef.current || !gridRef.current.api) return;

        const rowNode = gridRef.current.api.getRowNode(tick.tradeId);
        if (rowNode && rowNode.data) {
            const updatedData: Trade = {
                ...rowNode.data,
                marketRate: tick.marketRate,
                unrealizedPnl: tick.unrealizedPnl,
            };

            gridRef.current.api.applyTransaction({ update: [updatedData] });
            gridRef.current.api.flashCells({
                rowNodes: [rowNode],
                columns: ['marketRate', 'unrealizedPnl'],
            });
        }
    }, []);

    const { isConnected } = useValuationStream(handleValuationTick);

    const columnDefs = useMemo<ColDef<Trade>[]>(() => [
        { field: 'tradeRef', headerName: 'Reference', width: 180, pinned: 'left' },
        { field: 'currencyPair', headerName: 'Pair', width: 120 },
        {
            field: 'side',
            headerName: 'Side',
            width: 100,
            cellRenderer: (params: any) => (
                <span className={`font-semibold ${params.value === 'BUY' ? 'text-emerald-400' : 'text-rose-400'}`}>
          {params.value}
        </span>
            ),
        },
        { field: 'tradeType', headerName: 'Type', width: 110 },
        {
            field: 'notional',
            headerName: 'Notional',
            width: 150,
            valueFormatter: (p) => p.value?.toLocaleString(undefined, { minimumFractionDigits: 2 }),
        },
        {
            field: 'contractRate',
            headerName: 'Booked Rate',
            width: 130,
            valueFormatter: (p) => p.value?.toFixed(4),
        },
        {
            field: 'marketRate',
            headerName: 'Live Rate',
            width: 130,
            valueFormatter: (p) => p.value ? p.value.toFixed(4) : '---',
        },
        {
            field: 'unrealizedPnl',
            headerName: 'MTM P&L',
            width: 160,
            cellRenderer: (params: any) => {
                if (params.value === undefined || params.value === null) return <span>---</span>;
                const isProfitable = params.value >= 0;
                return (
                    <span className={`font-mono font-bold ${isProfitable ? 'text-emerald-400' : 'text-rose-400'}`}>
            {isProfitable ? '+' : ''}{params.value.toLocaleString(undefined, { minimumFractionDigits: 2 })}
          </span>
                );
            },
        },
        { field: 'status', headerName: 'Status', width: 120 },
    ], []);

    return (
        <div className="flex flex-col h-full bg-slate-900 border border-slate-800 rounded-lg p-4">
            <div className="flex justify-between items-center mb-4">
                <div>
                    <h2 className="text-xl font-bold text-slate-100">Live Trading Blotter</h2>
                    <p className="text-sm text-slate-400">Continuous MTM calculations streamed via Kafka</p>
                </div>
                <div className="flex items-center space-x-2">
                    <span className={`h-3 w-3 rounded-full ${isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
                    <span className="text-sm text-slate-300 font-mono">
            {isConnected ? 'STREAM ACTIVE' : 'RECONNECTING'}
          </span>
                </div>
            </div>

            <div className="w-full h-[550px]">
                <AgGridReact<Trade>
                    ref={gridRef}
                    rowData={initialTrades}
                    columnDefs={columnDefs}
                    getRowId={(params) => params.data.id}
                    animateRows={true}
                />
            </div>
        </div>
    );
};