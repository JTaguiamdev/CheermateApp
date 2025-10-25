import { MD3LightTheme, MD3DarkTheme } from 'react-native-paper';

// Custom color scheme matching Material Design 3
const colors = {
  primary: '#6200ee',
  primaryContainer: '#BB86FC',
  secondary: '#03dac6',
  secondaryContainer: '#018786',
  error: '#B00020',
  errorContainer: '#CF6679',
  success: '#38A169',
  warning: '#ED8936',
  background: '#FFFFFF',
  surface: '#FFFFFF',
  surfaceVariant: '#F5F5F5',
  onPrimary: '#FFFFFF',
  onSecondary: '#000000',
  onBackground: '#000000',
  onSurface: '#000000',
  outline: '#E0E0E0',
};

const darkColors = {
  primary: '#BB86FC',
  primaryContainer: '#3700B3',
  secondary: '#03dac6',
  secondaryContainer: '#03dac6',
  error: '#CF6679',
  errorContainer: '#B00020',
  success: '#48BB78',
  warning: '#F6AD55',
  background: '#121212',
  surface: '#1E1E1E',
  surfaceVariant: '#2C2C2C',
  onPrimary: '#000000',
  onSecondary: '#000000',
  onBackground: '#FFFFFF',
  onSurface: '#FFFFFF',
  outline: '#3C3C3C',
};

export const lightTheme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    ...colors,
  },
};

export const darkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    ...darkColors,
  },
};

// Priority colors
export const priorityColors = {
  High: '#E53E3E',
  Medium: '#ED8936',
  Low: '#38A169',
};

// Status colors
export const statusColors = {
  Pending: '#FFA500',
  InProgress: '#0066CC',
  Completed: '#38A169',
  Cancelled: '#808080',
  OverDue: '#E53E3E',
};

// Typography
export const typography = {
  h1: {
    fontSize: 32,
    fontWeight: '700' as const,
    lineHeight: 40,
  },
  h2: {
    fontSize: 28,
    fontWeight: '700' as const,
    lineHeight: 36,
  },
  h3: {
    fontSize: 24,
    fontWeight: '600' as const,
    lineHeight: 32,
  },
  h4: {
    fontSize: 20,
    fontWeight: '600' as const,
    lineHeight: 28,
  },
  body1: {
    fontSize: 16,
    fontWeight: '400' as const,
    lineHeight: 24,
  },
  body2: {
    fontSize: 14,
    fontWeight: '400' as const,
    lineHeight: 20,
  },
  caption: {
    fontSize: 12,
    fontWeight: '400' as const,
    lineHeight: 16,
  },
};

// Spacing
export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

// Border radius
export const borderRadius = {
  small: 4,
  medium: 8,
  large: 16,
  xl: 24,
};
