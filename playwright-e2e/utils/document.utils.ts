import type { UploadFile } from "../page-objects/pages/event-wizard.po";

/**
 * A one-page PDF built in memory, so the suite carries no binary fixtures and every upload has a
 * name unique to the test. dm-store and CDAM check the MIME type and the file's magic number,
 * not its rendering, but the document is well formed so the media viewer can open it too.
 */
export function pdfFile(name: string, text = name): UploadFile {
  const escaped = text.replace(/[\\()]/g, (c) => `\\${c}`);
  const stream = `BT /F1 12 Tf 72 720 Td (${escaped}) Tj ET`;
  const objects = [
    "<< /Type /Catalog /Pages 2 0 R >>",
    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
    `<< /Length ${stream.length} >>\nstream\n${stream}\nendstream`,
    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
  ];
  let body = "%PDF-1.4\n";
  const offsets: number[] = [];
  objects.forEach((object, index) => {
    offsets.push(body.length);
    body += `${index + 1} 0 obj\n${object}\nendobj\n`;
  });
  const xref = body.length;
  body += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  body += offsets.map((offset) => `${String(offset).padStart(10, "0")} 00000 n \n`).join("");
  body += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF\n`;
  return {
    name: name.endsWith(".pdf") ? name : `${name}.pdf`,
    mimeType: "application/pdf",
    buffer: Buffer.from(body, "latin1"),
  };
}
