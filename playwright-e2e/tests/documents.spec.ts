import { expect, test } from "../fixtures";
import { BAIL_EVENTS } from "../page-objects";
import { TabSnapshot, pdfFile, shownDateFromToday } from "../utils";

/**
 * Documents on a legal representative's submitted case.
 *
 * - "Upload documents" as the legal representative is ported from bails-upload-documents.feature,
 *   "Upload Documents after submitting".
 * - "Edit documents" as the admin officer is ported from bails-edit-documents.feature, "Delete,
 *   edit and add a document as a Judge". Despite its name that scenario runs as the admin officer
 *   (its Background switches to one and the scenario never switches again). Its precondition, a
 *   supporting-evidence document next to the generated application PDF, is uploaded over CCD.
 */
test.describe("Documents", () => {
  test(
    "uploads a document as the legal representative and lists it among the applicant documents",
    async ({ seedCase, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");
      const file = pdfFile(`BailsExampleDocument-${seeded.caseId}`);
      const description = `This is a bails document uploaded by the LR after submitting case ${seeded.caseId}.`;

      const casePage = await openCaseAs("legalRepBails", seeded.caseId);
      const wizard = await casePage.startWizard(BAIL_EVENTS.uploadDocuments);
      await wizard.completeToCheckYourAnswers({
        uploadDocuments: [
          { addNew: "uploadDocuments" },
          { upload: "uploadDocuments_0_document", file },
          { fill: "uploadDocuments_0_description", value: description },
        ],
      });
      await expect(wizard.page.getByText(file.name, { exact: true })).toBeVisible();
      await expect(wizard.page.getByText(description, { exact: true })).toBeVisible();
      await wizard.submit();
      await expect(
        wizard.confirmation("The document(s) you uploaded are available to view in the documents tab.")
      ).toBeVisible();
      await wizard.closeAndReturnToCase();

      const after = await openCaseAs("legalRepBails", seeded.caseId);
      const documents = await after.snapshotTab("Documents");
      expect(documentItem(documents, "applicantDocumentsWithMetadata", file.name)).toMatchObject({
        Description: description,
        "Date uploaded": shownDateFromToday(0),
      });
    }
  );

  test(
    "deletes, edits and adds applicant documents as the admin officer and records what changed in a case note",
    async ({ seedCase, progress, ccdApiAs, openCaseAs }) => {
      const seeded = await seedCase("legalRepBails");
      const evidence = `GroundsForBailSupportingEvidence-${seeded.caseId}.pdf`;
      await progress(seeded.caseId).uploadDocument("legalRepBails", evidence, "Supporting evidence");

      // The page lists the documents in the order CCD holds them, so the evidence's position is
      // read from the case rather than assumed.
      const { data } = await (await ccdApiAs("adminOfficer")).getCase(seeded.caseId);
      const names = (
        data.applicantDocumentsWithMetadata as { value: { document: { document_filename: string } } }[]
      ).map((item) => item.value.document.document_filename);
      const deleted = names.indexOf(evidence);
      const applicationPdf = names.find((name) => /-bail-application-details\.PDF$/.test(name));
      expect(deleted, `applicant documents on the case: ${names.join(", ")}`).toBeGreaterThanOrEqual(0);
      expect(names, "applicant documents on the case").toHaveLength(2);
      const added = pdfFile(`Evidence-${seeded.caseId}`);
      const editedDescription = `This is a new description that wasn't here before, on case ${seeded.caseId}`;
      const addedDescription = `This is a new document that wasn't here before, on case ${seeded.caseId}`;
      const reason = `This is a reason for editing, removing and adding documents on case ${seeded.caseId}`;

      const casePage = await openCaseAs("adminOfficer", seeded.caseId);
      const wizard = await casePage.startWizard(BAIL_EVENTS.editDocuments);
      // After the removal the remaining document is item 0 and the new one item 1.
      await wizard.completeToCheckYourAnswers({
        editBailDocuments: [
          { removeItem: `applicantDocumentsWithMetadata_${deleted}` },
          { fill: "applicantDocumentsWithMetadata_0_description", value: editedDescription },
          { addNew: "applicantDocumentsWithMetadata" },
          { fill: "applicantDocumentsWithMetadata_1_suppliedBy", value: "Somebody testing this" },
          { upload: "applicantDocumentsWithMetadata_1_document", file: added },
          { fill: "applicantDocumentsWithMetadata_1_description", value: addedDescription },
          {
            date: "dateUploaded",
            withinItem: "applicantDocumentsWithMetadata_1",
            value: { day: "10", month: "02", year: "2022" },
          },
        ],
        editReasons: [{ fill: "editDocumentsReason", value: reason }],
      });
      await wizard.submit();

      const after = await openCaseAs("adminOfficer", seeded.caseId);
      const documents = await after.snapshotTab("Documents");
      expect(documentItem(documents, "applicantDocumentsWithMetadata", evidence), "the deleted document").toBeUndefined();
      expect(documentItem(documents, "applicantDocumentsWithMetadata", applicationPdf ?? "")).toMatchObject({
        Description: editedDescription,
      });
      expect(documentItem(documents, "applicantDocumentsWithMetadata", added.name)).toMatchObject({
        "Supplied by": "Somebody testing this",
        Description: addedDescription,
        "Date uploaded": "10 Feb 2022",
      });

      const notes = await after.snapshotTab("Case notes");
      expect(notes).toMatchObject({
        "caseNotes › Case notes 1 › Subject": "A document was edited or deleted",
      });
      expect(notes["caseNotes › Case notes 1 › Case note"]).toContain(
        `Document names: [${evidence}, ${applicationPdf}, ${added.name}]`
      );
      expect(notes["caseNotes › Case notes 1 › Case note"]).toContain(`Reason: ${reason}`);
    }
  );
});

/**
 * The rows of the document-collection item whose Document is `fileName`, keyed by their labels
 * ("Document", "Description", "Date uploaded", ...), or undefined when no item holds that file.
 */
function documentItem(
  snapshot: TabSnapshot,
  collection: string,
  fileName: string
): Record<string, string> | undefined {
  const documentKey = Object.keys(snapshot).find(
    (key) => key.startsWith(`${collection} › `) && key.endsWith(" › Document") && snapshot[key] === fileName
  );
  if (!documentKey) return undefined;
  const prefix = documentKey.slice(0, -"Document".length);
  return Object.fromEntries(
    Object.entries(snapshot)
      .filter(([key]) => key.startsWith(prefix))
      .map(([key, value]) => [key.slice(prefix.length), value])
  );
}
