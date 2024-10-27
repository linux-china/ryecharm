package insyncwithfoo.ryecharm.uv.actions

import com.intellij.openapi.components.Service
import insyncwithfoo.ryecharm.CoroutineService
import kotlinx.coroutines.CoroutineScope


@Service(Service.Level.PROJECT)
internal class ActionCoroutine(override val scope: CoroutineScope) : CoroutineService
