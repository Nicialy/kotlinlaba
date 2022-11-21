package ru.ac.uniyar.models

import org.http4k.core.Uri
import org.http4k.template.ViewModel

class ShowErrorInfoVM(val uri: Uri, val message: String?) : ViewModel
