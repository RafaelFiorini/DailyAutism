import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('App Component', () => {
  it('renders initial application title or entry point', () => {
    render(<App />);
    expect(screen.getByText(/DailyAutism/i)).toBeInTheDocument();
  });
});

