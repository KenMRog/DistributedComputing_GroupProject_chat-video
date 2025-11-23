// Small helpers for formatting timestamps consistently with user's local timezone
// Parse timestamp reliably. Backend uses LocalDateTime (no timezone) which serializes like
// "2025-11-11T12:34:56". `new Date(string)` can be inconsistent across browsers for
// timezone-less ISO strings, so detect that format and create a local Date explicitly.
function parseTimestampToLocalDate(ts) {
  if (!ts) return null;
  // If it's already a Date
  if (ts instanceof Date) return ts;

  // If it's a numeric timestamp (ms)
  if (typeof ts === 'number') return new Date(ts);

  // If string contains timezone info (Z or +HH:MM/-HH:MM), let Date parse it
  if (typeof ts === 'string' && /([zZ]|[+-]\d{2}:?\d{2})$/.test(ts)) {
    const d = new Date(ts);
    return isNaN(d.getTime()) ? null : d;
  }

  // Match yyyy-mm-ddTHH:MM:SS(.sss)? with no timezone
  const match = typeof ts === 'string' && ts.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(\.\d+)?)?$/);
  if (match) {
    const year = parseInt(match[1], 10);
    const month = parseInt(match[2], 10) - 1;
    const day = parseInt(match[3], 10);
    const hour = parseInt(match[4], 10);
    const minute = parseInt(match[5], 10);
    const second = match[6] ? parseInt(match[6], 10) : 0;
    const ms = match[7] ? Math.round(parseFloat(match[7]) * 1000) : 0;
    // Treat backend LocalDateTime (no timezone) as UTC so clients convert to their local timezone.
    const utcMillis = Date.UTC(year, month, day, hour, minute, second, ms);
    return new Date(utcMillis);
  }

  // Fallback: let Date try to parse
  const d = new Date(ts);
  return isNaN(d.getTime()) ? null : d;
}

export function formatTime(timestamp) {
  const d = parseTimestampToLocalDate(timestamp);
  if (!d) return '';
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function formatDateTimeLocal(timestamp) {
  const d = parseTimestampToLocalDate(timestamp);
  if (!d) return '';
  const date = d.toLocaleDateString([], { month: 'short', day: 'numeric' });
  const time = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  return `${date}, ${time}`;
}

export function formatDateShort(timestamp) {
  const d = parseTimestampToLocalDate(timestamp);
  if (!d) return '';
  return d.toLocaleDateString([], { month: 'short', day: 'numeric' });
}

export function formatWeekday(timestamp) {
  const d = parseTimestampToLocalDate(timestamp);
  if (!d) return '';
  return d.toLocaleDateString([], { weekday: 'short' });
}
