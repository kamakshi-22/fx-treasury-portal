import React, { useState } from 'react';
import type {CurrencyPair, TradeSide, TradeType, CreateTradePayload, Trade} from '../../types';
import { submitTrade } from '../../services/api';

interface DealCaptureModalProps {
    isOpen: boolean;
    onClose: () => void;
    onTradeCreated: (newTrade: Trade) => void;
}

export const DealCaptureModal: React.FC<DealCaptureModalProps> = ({ isOpen, onClose, onTradeCreated }) => {
    const [formData, setFormData] = useState<CreateTradePayload>({
        currencyPair: 'USD_INR',
        side: 'BUY',
        tradeType: 'SPOT',
        notional: 100000,
        contractRate: 83.25,
        valueDate: new Date(Date.now() + 86400000 * 2).toISOString().split('T')[0] + 'T10:00:00Z',
    });
    const [isSubmitting, setIsSubmitting] = useState(false);

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const created = await submitTrade(formData);
            onTradeCreated(created);
            onClose();
        } catch (err) {
            console.error('Failed to submit trade', err);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex justify-center items-center z-50">
            <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 w-full max-w-md shadow-2xl">
                <h3 className="text-lg font-bold text-white mb-4">Book New FX Transaction</h3>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">Currency Pair</label>
                        <select
                            value={formData.currencyPair}
                            onChange={(e) => setFormData({ ...formData, currencyPair: e.target.value as CurrencyPair })}
                            className="w-full bg-slate-900 border border-slate-700 text-white rounded p-2 text-sm focus:border-sky-500 focus:outline-none"
                        >
                            <option value="USD_INR">USD/INR</option>
                            <option value="EUR_USD">EUR/USD</option>
                            <option value="GBP_USD">GBP/USD</option>
                            <option value="USD_JPY">USD/JPY</option>
                        </select>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-semibold text-slate-400 mb-1">Side</label>
                            <select
                                value={formData.side}
                                onChange={(e) => setFormData({ ...formData, side: e.target.value as TradeSide })}
                                className="w-full bg-slate-900 border border-slate-700 text-white rounded p-2 text-sm"
                            >
                                <option value="BUY">BUY</option>
                                <option value="SELL">SELL</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-xs font-semibold text-slate-400 mb-1">Trade Type</label>
                            <select
                                value={formData.tradeType}
                                onChange={(e) => setFormData({ ...formData, tradeType: e.target.value as TradeType })}
                                className="w-full bg-slate-900 border border-slate-700 text-white rounded p-2 text-sm"
                            >
                                <option value="SPOT">SPOT</option>
                                <option value="FORWARD">FORWARD</option>
                            </select>
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">Notional Amount</label>
                        <input
                            type="number"
                            value={formData.notional}
                            onChange={(e) => setFormData({ ...formData, notional: parseFloat(e.target.value) })}
                            className="w-full bg-slate-900 border border-slate-700 text-white rounded p-2 text-sm focus:border-sky-500 focus:outline-none"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">Contract Rate</label>
                        <input
                            type="number"
                            step="0.0001"
                            value={formData.contractRate}
                            onChange={(e) => setFormData({ ...formData, contractRate: parseFloat(e.target.value) })}
                            className="w-full bg-slate-900 border border-slate-700 text-white rounded p-2 text-sm focus:border-sky-500 focus:outline-none"
                            required
                        />
                    </div>

                    <div className="flex justify-end space-x-3 pt-4 border-t border-slate-700">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-slate-200 text-sm font-semibold rounded"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white text-sm font-semibold rounded transition"
                        >
                            {isSubmitting ? 'Booking...' : 'Submit Deal'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};