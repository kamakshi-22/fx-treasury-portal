// Currency pairs supported by the backend
export type CurrencyPair = 'EUR_USD' | 'USD_INR' | 'GBP_USD' | 'USD_JPY';

// Trade parameters matching TradeSide and TradeType enums
export type TradeSide = 'BUY' | 'SELL';
export type TradeType = 'SPOT' | 'FORWARD';
export type TradeStatus = 'BOOKED' | 'SETTLED' | 'CANCELLED';

// Full trade record representation
export interface Trade {
    id: string;
    tradeRef: string;
    currencyPair: CurrencyPair;
    side: TradeSide;
    tradeType: TradeType;
    notional: number;
    contractRate: number;
    marketRate?: number;
    unrealizedPnl?: number;
    status: TradeStatus;
    tradeDate: string;
    valueDate: string;
    createdAt: string;
}

// Event received from the valuation stream
export interface ValuationEvent {
    tradeId: string;
    tradeRef: string;
    currencyPair: CurrencyPair;
    contractRate: number;
    marketRate: number;
    unrealizedPnl: number;
    valuationTimestamp: string;
}

// Payload sent to POST /api/v1/trades
export interface CreateTradePayload {
    currencyPair: CurrencyPair;
    side: TradeSide;
    tradeType: TradeType;
    notional: number;
    contractRate: number;
    valueDate: string;
}