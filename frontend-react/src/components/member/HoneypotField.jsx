export default function HoneypotField({ value, onChange }) {
  return (
    <div className="hx-hp" aria-hidden="true">
      <label htmlFor="website">Website</label>
      <input
        id="website"
        type="text"
        name="website"
        autoComplete="off"
        tabIndex={-1}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  )
}
