import { AlertCircle, CheckCircle2, Info } from 'lucide-react';

const icons = { error: AlertCircle, success: CheckCircle2, info: Info };

export default function Notice({ tone = 'info', children }) {
  const Icon = icons[tone] || Info;
  return <div className={`notice notice--${tone}`} role={tone === 'error' ? 'alert' : 'status'}><Icon size={18} aria-hidden="true" /><span>{children}</span></div>;
}
