' it is very useful to press shortcut Shift+F1 to reveal formatting

Sub fc_init()
    fc_page_setup
    fc_set_page_number_at_footer
    fc_normal_paragraphs_set_spacing
End Sub

Sub fc_init_for_code()
    fc_page_setup
    fc_set_page_number_at_footer
    fc_normal_paragraphs_set_spacing_for_code
    fc_normal_paragraphs_set_font_for_code
End Sub

Sub fc_page_setup()
    ' page Orientation
    ActiveDocument.PageSetup.Orientation = wdOrientPortrait ' wdOrientLandscape
    
    ' narrow margins by default
    ActiveDocument.PageSetup.LeftMargin = InchesToPoints(0.5)
    ActiveDocument.PageSetup.RightMargin = InchesToPoints(0.5)
    ActiveDocument.PageSetup.TopMargin = InchesToPoints(0.5)
    ActiveDocument.PageSetup.BottomMargin = InchesToPoints(0.5)
    
    ' footer distance
    ActiveDocument.PageSetup.FooterDistance = InchesToPoints(0.4)
End Sub

Sub fc_set_page_number_at_footer()
    With ActiveDocument.Sections(1).Footers(wdHeaderFooterPrimary)
        .PageNumbers.Add
        .Range.Font.Size = 9
    End With
End Sub

Sub fc_normal_paragraphs_set_spacing()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style = "Normal" Or p.Style = "norm" Or p.Style = "norm-static" Or p.Style = "norm-dynamic" Or p.Style = "norm-error" Then
            p.SpaceBeforeAuto = False
            p.SpaceBefore = 0
            p.SpaceAfterAuto = False
            If p.Range.ListFormat.ListType <> 0 Or p.Range.Information(wdWithInTable) Then
                p.SpaceAfter = 0
            Else
                p.SpaceAfter = 3
            End If
            p.LineSpacing = 1
            p.LineSpacingRule = wdLineSpaceAtLeast
        End If
    Next
End Sub

' Note: excluding paragraph in bullet and table
Sub fc_normal_paragraphs_set_indent()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If (p.Style = "Normal" Or p.Style = "norm" Or p.Style = "norm-static" Or p.Style = "norm-dynamic") _
            And (p.Range.ListFormat.ListType = 0) _
            And (Not p.Range.Information(wdWithInTable)) Then
            'p.FirstLineIndent = InchesToPoints(0.2)
            p.LeftIndent = 0
            p.RightIndent = 0
        End If
    Next
End Sub

Sub fc_normal_paragraphs_set_spacing_for_code()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style = "Normal" Or p.Style = "norm" Or p.Style = "norm-static" Or p.Style = "norm-dynamic" Or p.Style = "norm-error" Then
            p.SpaceBeforeAuto = False
            p.SpaceBefore = 0
            p.SpaceAfterAuto = False
        p.SpaceAfter = 0
            p.LineSpacing = 10
            p.LineSpacingRule = wdLineSpaceAtLeast
        End If
    Next
End Sub

Sub fc_normal_paragraphs_set_font_for_code()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style = "Normal" Or p.Style = "norm" Or p.Style = "norm-static" Or p.Style = "norm-dynamic" Or p.Style = "norm-error" Then
        p.Range.Font.Name = "Courier New"
        p.Range.Font.Size = 9
        End If
    Next
End Sub

Sub fc_heading_paragraphs_set_spacing_and_indent()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style Like "Heading *" Then
            p.Style = p.Style
            p.SpaceBeforeAuto = False
            p.SpaceBefore = 6
            p.SpaceAfterAuto = False
            p.SpaceAfter = 6
            p.LineSpacing = 12
            p.LeftIndent = 0
            p.RightIndent = 0
            p.FirstLineIndent = 0
        End If
    Next
End Sub

Sub fc_normal_paragraphs_merge_broken()
    For i = 1 To ActiveDocument.Paragraphs.count
        Do While (i < ActiveDocument.Paragraphs.count)
            Dim nextParagraph As Paragraph
            Set nextParagraph = ActiveDocument.Paragraphs(i + 1)
            If fc_is_source_code_paragraph(nextParagraph.Range.Font.Name) Then
                Exit Do '' break Do ... Loop if next paragraph is a source code paragraph
            End If
            Dim n As Integer
            n = ActiveDocument.Paragraphs.count
            fc_merge_paragraph ActiveDocument.Paragraphs(i)
            If n = ActiveDocument.Paragraphs.count Then
                Exit Do ' break Do ... Loop since there is no paragraph merged.
            End If
        Loop
    Next i
End Sub

Private Sub fc_merge_paragraph(p As Paragraph)
    If Not fc_is_normal_paragraph(p) Then
        Exit Sub
    End If
    
    If fc_is_source_code_paragraph(p.Range.Font.Name) Then
        Exit Sub
    End If
    
    If EndsWith(p.Range.text, ".") Then
        Exit Sub
    End If
        
    ' select this paragraph and replace paragraph terminator char with a space.
    p.Range.Select
    With Selection.Find
       .text = "^p"
       .Replacement.text = " "
       .Forward = True
       .Wrap = wdFindStop
    End With
    Selection.Find.Execute Replace:=wdReplaceAll
    Selection.EndOf
End Sub

Sub fc_note_paragraphs_change_style()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style = "note" Then
            If p.Range.Font.Size < 8 Then
                p.Range.Font.Grow
            End If
            p.SpaceBeforeAuto = False
            p.SpaceBefore = 0
            p.SpaceAfterAuto = False
            p.SpaceAfter = 3
            p.LineSpacing = 10
            p.LeftIndent = InchesToPoints(0.3)
            p.RightIndent = 0
        End If
    Next
End Sub

Sub tmp_change_heading5_to_bold()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style = "Heading 4" Then
            p.Style = "Normal"
            p.Range.Font.Bold = True
            p.Range.Font.Size = 10
        End If
        If p.Style = "Heading 3" Then
            p.LeftIndent = 0
        End If
    Next
End Sub

Sub tmp_html_preformatted()
    Dim p As Paragraph
    For Each p In ActiveDocument.Paragraphs
        If p.Style = "HTML Preformatted" Then
            p.Style = "HTML Preformatted"
            p.LeftIndent = InchesToPoints(0.05)
            p.Range.Font.Size = 9
            p.LineSpacing = 12
        End If
    Next
End Sub



Public Function EndsWith(str As String, ending As String) As Boolean
    str = Replace(str, Chr(13), "")
    str = Trim(UCase(str))

    Dim endingLen As Integer
    endingLen = Len(ending)
    EndsWith = (Right(str, endingLen) = UCase(ending))
End Function

Public Function StartsWith(str As String, start As String) As Boolean
     Dim startLen As Integer
     startLen = Len(start)
     StartsWith = (Left(Trim(UCase(str)), startLen) = UCase(start))
End Function

Public Function fc_is_normal_paragraph(p As Paragraph) As Boolean
    fc_is_normal_paragraph = (p.Style = "Normal" Or p.Style = "norm" Or p.Style = "norm-static" Or p.Style = "norm-dynamic") _
            And (p.Range.ListFormat.ListType = 0) _
            And (Not p.Range.Information(wdWithInTable))
End Function

Private Function fc_is_source_code_paragraph(fontName As String) As Boolean
    fc_is_source_code_paragraph = (fontName = "Courier New" _
                                Or fontName = "TheSansMonoCondensed-SemiLight")
End Function

Private Function fc_is_possible_title_paragraph(p As Paragraph) As Boolean
    fc_is_possible_title_paragraph = p.Range.Font.Size >= 14
End Function

Sub RemoveTextbox()
'
' RemoveTextbox Macro
'
'
    Dim shp As Shape
    Dim oRngAnchor As Range
    Dim sString As String

    For Each shp In ActiveDocument.Shapes
        If shp.Type <> msoPicture Then
            MsgBox ("shape type is not picture")
            If shp.Type = msoTextBox Then
                ' copy text to string, without last paragraph mark
                sString = Left(shp.TextFrame.TextRange.text, _
                  shp.TextFrame.TextRange.Characters.count - 1)
                If Len(sString) > 0 Then
                    ' set the range to insert the text
                    Set oRngAnchor = shp.Anchor.Paragraphs(1).Range
                    ' insert the textbox text before the range object
                    oRngAnchor.InsertBefore _
                      "Textbox start << " & sString & " >> Textbox end"
                End If
                shp.Delete
            End If
        End If
    Next shp
End Sub



Sub turn_off_spelling_check()
    ActiveDocument.SpellingChecked = False
    ActiveDocument.Range.NoProofing = False
    ActiveDocument.ShowSpellingErrors = False
End Sub

Sub Foo()
    'MsgBox ActiveDocument.Paragraphs(1).Range.Font.Size
    fc_normal_paragraphs_merge_unfinished
    

    'fc_merge_paragraph2 ActiveDocument.Paragraphs(1)
    'fc_merge_paragraph2 ActiveDocument.Paragraphs(2)
    'fc_merge_paragraph ActiveDocument.Paragraphs(1)
    'MsgBox ActiveDocument.Paragraphs(1).Range.text
        
    'MsgBox text
    'all_tables_set_table_style
    'normal_paragraphs_set_spacing
    'normal_paragraphs_set_indent
    'note_paragraphs_change_style
    'tmp_change_heading5_to_bold
End Sub
