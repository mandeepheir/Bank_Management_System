
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  BankService,
  BankAccount,
  BankTransaction
} from './services/bank.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  accounts: BankAccount[] = [];
  transactions: BankTransaction[] = [];

  selectedAccountId: number | null = null;

  depositAmount: number | null = null;
  withdrawalAmount: number | null = null;

  newAccountHolderName = '';
  newAccountNumber = '';
  newAccountBalance: number | null = 0;

  isLoading = false;
  isProcessing = false;
  isCreatingAccount = false;
  isLoadingTransactions = false;

  errorMessage = '';
  successMessage = '';
  transactionError = '';

  constructor(private bankService: BankService) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  get selectedAccount(): BankAccount | undefined {
    return this.accounts.find(
      account => account.id === Number(this.selectedAccountId)
    );
  }

  get totalBalance(): number {
    return this.accounts.reduce(
      (total, account) => total + Number(account.balance),
      0
    );
  }

  get totalTransactions(): number {
    return this.transactions.length;
  }

  loadAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.bankService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;

        if (
          this.selectedAccountId === null ||
          !accounts.some(
            account => account.id === Number(this.selectedAccountId)
          )
        ) {
          this.selectedAccountId = accounts.length
            ? accounts[0].id
            : null;
        }

        this.isLoading = false;
        this.loadTransactions();
      },
      error: () => {
        this.errorMessage =
          'Could not load accounts. Check that the backend is running.';
        this.isLoading = false;
      }
    });
  }

  onAccountChange(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.loadTransactions();
  }

  loadTransactions(): void {
    if (this.selectedAccountId === null) {
      this.transactions = [];
      this.isLoadingTransactions = false;
      this.transactionError = '';
      return;
    }

    this.isLoadingTransactions = true;
    this.transactionError = '';

    this.bankService
      .getTransactions(Number(this.selectedAccountId))
      .subscribe({
        next: (transactions) => {
          this.transactions = transactions;
          this.isLoadingTransactions = false;
        },
        error: () => {
          this.transactions = [];
          this.transactionError =
            'Could not load transaction history.';
          this.isLoadingTransactions = false;
        }
      });
  }

  createAccount(): void {
    this.errorMessage = '';
    this.successMessage = '';

    const holderName = this.newAccountHolderName.trim();
    const accountNumber = this.newAccountNumber.trim();
    const balance = this.newAccountBalance;

    if (!holderName || !accountNumber) {
      this.errorMessage =
        'Please enter the account holder name and account number.';
      return;
    }

    if (
      balance === null ||
      !Number.isFinite(balance) ||
      balance < 0
    ) {
      this.errorMessage =
        'Starting balance must be zero or greater.';
      return;
    }

    this.isCreatingAccount = true;

    this.bankService.createAccount({
      accountHolderName: holderName,
      accountNumber,
      balance
    }).subscribe({
      next: (createdAccount) => {
        this.newAccountHolderName = '';
        this.newAccountNumber = '';
        this.newAccountBalance = 0;

        // Select the newly created account.
        this.selectedAccountId = createdAccount.id;

        this.successMessage =
          `Account for ${createdAccount.accountHolderName} was created successfully.`;

        this.isCreatingAccount = false;
        this.loadAccounts();
      },
      error: (error) => {
        this.errorMessage =
          error.status === 409
            ? 'That account number already exists. Use a different number.'
            : error.error?.message ||
              'Could not create the account. Please try again.';

        this.isCreatingAccount = false;
      }
    });
  }

  deposit(): void {
    this.submitTransaction('deposit');
  }

  withdraw(): void {
    this.submitTransaction('withdraw');
  }

  private submitTransaction(
    type: 'deposit' | 'withdraw'
  ): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.selectedAccountId === null) {
      this.errorMessage = 'Please select a bank account first.';
      return;
    }

    const amount =
      type === 'deposit'
        ? this.depositAmount
        : this.withdrawalAmount;

    if (
      amount === null ||
      !Number.isFinite(amount) ||
      amount <= 0
    ) {
      this.errorMessage =
        'Please enter an amount greater than zero.';
      return;
    }

    this.isProcessing = true;

    const request$ =
      type === 'deposit'
        ? this.bankService.deposit(
            Number(this.selectedAccountId),
            amount
          )
        : this.bankService.withdraw(
            Number(this.selectedAccountId),
            amount
          );

    request$.subscribe({
      next: () => {
        this.successMessage =
          type === 'deposit'
            ? 'Deposit completed successfully.'
            : 'Withdrawal completed successfully.';

        this.depositAmount = null;
        this.withdrawalAmount = null;
        this.isProcessing = false;

        this.loadAccounts();
      },
      error: (error) => {
        this.errorMessage =
          error.error?.message ||
          `The ${type} could not be completed. Please check the amount and try again.`;

        this.isProcessing = false;
      }
    });
  }
}
