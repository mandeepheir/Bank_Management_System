
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BankAccount {
  id: number;
  accountHolderName: string;
  accountNumber: string;
  balance: number;
}

export interface BankTransaction {
  id: number;
  accountId: number;
  amount: number;
  type: string;
  transactionDate: string;
}

export interface TransactionRequest {
  amount: number;
}

export interface CreateAccountRequest {
  accountHolderName: string;
  accountNumber: string;
  balance: number;
}

@Injectable({
  providedIn: 'root'
})
export class BankService {
  private readonly apiUrl = 'http://localhost:8080/api/bank';

  constructor(private http: HttpClient) {}

  // Get all bank accounts.
  getAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(
      `${this.apiUrl}/accounts`
    );
  }

  // Get one bank account.
  getAccount(id: number): Observable<BankAccount> {
    return this.http.get<BankAccount>(
      `${this.apiUrl}/accounts/${id}`
    );
  }

  // Get transaction history.
  getTransactions(accountId: number): Observable<BankTransaction[]> {
    return this.http.get<BankTransaction[]>(
      `${this.apiUrl}/accounts/${accountId}/transactions`
    );
  }

  // Create a new bank account.
  createAccount(
    account: CreateAccountRequest
  ): Observable<BankAccount> {
    return this.http.post<BankAccount>(
      `${this.apiUrl}/accounts`,
      account
    );
  }

  // Deposit money.
  deposit(accountId: number, amount: number): Observable<unknown> {
    const request: TransactionRequest = { amount };

    return this.http.post(
      `${this.apiUrl}/accounts/${accountId}/deposit`,
      request
    );
  }

  // Withdraw money.
  withdraw(accountId: number, amount: number): Observable<unknown> {
    const request: TransactionRequest = { amount };

    return this.http.post(
      `${this.apiUrl}/accounts/${accountId}/withdraw`,
      request
    );
  }
}
