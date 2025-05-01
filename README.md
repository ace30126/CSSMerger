# CSSMerger

A tool to merge two CSS files and visually resolve conflicting style rules.

## Overview

CSSMerger is a desktop application designed to assist web developers and analysts by merging multiple CSS files into one or helping determine the priority of conflicting style rules. When multiple CSS files are applied to a webpage, complex CSS specificity and cascade rules can make it difficult to determine which style is ultimately applied.

This tool helps by allowing you to:
* Compare two CSS files side-by-side.
* Explicitly set the priority when rules (based on selectors) conflict between the two files.
* Preview which rules will be included in the final merged output.
* Combine separated CSS files into a single file, which can be useful for deployment or maintenance.

**Key Features:**

* Load and compare two CSS files.
* Visual lists displaying CSS rules from each file.
* Conflict resolution via a simple "Prioritize this CSS" option.
* Highlights the rules selected for the final merged output (green/bold text).
* Generates a single, merged CSS file based on selections.
* Supports drag-and-drop or file chooser dialogs for loading files.

## Installation

As this is a standalone executable (`CSSMerger.exe`), no formal installation is required. Simply download the file and run it.

**Requirement:** This application requires **Java Runtime Environment (JRE) version 1.8.0 or higher** to be installed on your system.

## Usage

1.  **Launch the Application:** Double-click `CSSMerger.exe`.
2.  **Load CSS Files:** Load two CSS files into the application using one of these methods:
    * Drag and drop a CSS file onto the designated area in the left panel, and another CSS file onto the area in the right panel.
    * Alternatively, click the "Select File..." button within each panel (left and right) to browse and choose a CSS file.
3.  **Review Rules:** The CSS rules identified within each loaded file will be displayed in the list view of their respective panels.
4.  **Resolve Conflicts:** If the same CSS selector exists in both files, a conflict occurs. Decide which file's rule should take precedence for these conflicts by selecting the **"Prioritize this CSS"** checkbox associated with the panel (left or right) whose rules you want to keep.
5.  **Preview Merge:** The rules that are selected to be included in the final merged output (either non-conflicting rules or the prioritized conflicting rules) will be visually highlighted in the lists (e.g., displayed in green and/or bold text). Review these highlighted rules.
6.  **Save Merged File:** Once you are satisfied with the selections and the preview, click the **"Save"** button. This will allow you to specify a name and location to save the newly generated merged CSS file.

## Built With

* [JAVA](https://www.java.com/) - The programming language used (Requires JRE 1.8.0+).
* [Swing](https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html) - The GUI toolkit for Java.

## Author

* REDUCTO (https://tutoreducto.tistory.com/672)

## Creation Date

* May 1, 2025 (1 day of development)

## Version

* v1.0

## Notes

* **Distribution:** Unauthorized distribution of this program is prohibited. Please leave a comment if you are interested in using or sharing it.
* **Customization:** If you need custom features or modifications, please leave a comment to discuss.

## Acknowledgements

* This program was created with the assistance of Gemini.
