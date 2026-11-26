/**
 * What a case tab shows, as `field path -> rendered text`.
 *
 * Each top-level field on a CCD tab is rendered in an element whose id is
 * `case-viewer-field-read--<fieldId>`, so the field ID comes from the definition rather than
 * from layout. Complex fields (addresses, interpreter languages, collections of notes) are
 * flattened one row per sub-field, keyed by the field ID, the nested panel titles and the
 * sub-field's label, e.g. `applicantAddress › Address › Postcode/Zipcode`. A lost postcode then
 * shows up as that one key changing rather than as a long string that differs somewhere.
 */
export type TabSnapshot = Record<string, string>;

/** Snapshots of several tabs, keyed by tab name. */
export type CaseViewSnapshot = Record<string, TabSnapshot>;

export const FIELD_ID_PREFIX = "case-viewer-field-read--";

/**
 * Runs in the browser against the active tab panel. Fields hidden by a show condition stay in
 * the DOM with a `hidden` attribute, so only visible elements are read.
 */
export function readTabSnapshot(panel: Element, fieldIdPrefix: string): TabSnapshot {
  const normalise = (text: string) => text.replace(/\s+/g, " ").trim();
  const snapshot: Record<string, string> = {};
  const put = (key: string, value: string) => {
    let unique = key;
    for (let n = 2; unique in snapshot; n++) unique = `${key} #${n}`;
    snapshot[unique] = value;
  };

  for (const field of panel.querySelectorAll(`[id^="${fieldIdPrefix}"]`)) {
    if (!field.checkVisibility()) continue;
    const fieldId = field.id.slice(fieldIdPrefix.length);
    const rows = [...field.querySelectorAll("tr.complex-panel-simple-field")].filter((row) =>
      row.checkVisibility()
    );
    if (rows.length === 0) {
      put(fieldId, normalise((field as HTMLElement).innerText));
      continue;
    }
    for (const row of rows) {
      const titles: string[] = [];
      for (
        let panelEl = row.closest(".complex-panel");
        panelEl && field.contains(panelEl);
        panelEl = panelEl.parentElement?.closest(".complex-panel") ?? null
      ) {
        const title = panelEl.querySelector(":scope > .complex-panel-title");
        titles.unshift(title ? normalise((title as HTMLElement).innerText) : "");
      }
      const label = normalise((row.querySelector("th") as HTMLElement | null)?.innerText ?? "");
      const value = normalise((row.querySelector("td") as HTMLElement | null)?.innerText ?? "");
      put([fieldId, ...titles.filter(Boolean), label].join(" › "), value);
    }
  }
  return snapshot;
}

export interface SnapshotChanges {
  /**
   * Keys (or key prefixes ending in " › ", or whole field IDs) the event is expected to add or
   * change. Anything else that differs is reported.
   */
  expected?: string[];
}

/**
 * Lists every difference between two snapshots of the same tab that the event was not expected
 * to make, one readable line per field: what it showed before and what it shows now.
 */
export function diffSnapshots(
  before: TabSnapshot,
  after: TabSnapshot,
  changes: SnapshotChanges = {}
): string[] {
  const isExpected = (key: string) =>
    (changes.expected ?? []).some(
      (allowed) => key === allowed || key.startsWith(`${allowed} › `) || key.startsWith(`${allowed} #`)
    );
  const differences: string[] = [];
  for (const key of new Set([...Object.keys(before), ...Object.keys(after)])) {
    if (before[key] === after[key] || isExpected(key)) continue;
    if (!(key in after)) {
      differences.push(`${key}: was ${JSON.stringify(before[key])}, now not shown`);
    } else if (!(key in before)) {
      differences.push(`${key}: was not shown, now ${JSON.stringify(after[key])}`);
    } else {
      differences.push(
        `${key}: was ${JSON.stringify(before[key])}, now ${JSON.stringify(after[key])}`
      );
    }
  }
  return differences;
}
