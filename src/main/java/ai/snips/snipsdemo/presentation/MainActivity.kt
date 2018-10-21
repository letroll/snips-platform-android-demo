package ai.snips.snipsdemo.presentation

import ai.snips.snipsdemo.R
import ai.snips.snipsdemo.business.PlateformState
import ai.snips.snipsdemo.business.ensurePermissions
import ai.snips.snipsdemo.observe
import ai.snips.snipsdemo.removeObservers
import ai.snips.snipsdemo.service.SnipsService
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeUIChange()
        setupUI()
        bindService(SnipsService.intent(this), presenter, 0)
    }

    private fun observeUIChange() {
        presenter = ViewModelProviders.of(this).get(MainPresenter::class.java)
        observe(presenter.serviceWorking) { getServiceStatusObserver() }
        setupServiceLoadingUI()
    }

    private fun getServiceStatusObserver(): Observer<Boolean> {
        return Observer {
            if (it) {
                observeSnipsUpdate()
            } else {
                stopObservingSnipsUpdate()
            }
        }
    }

    private fun stopObservingSnipsUpdate() {
        removeObservers(presenter.snipsService.plateFormState)
        removeObservers(presenter.snipsService.snipsPlatformClientStatus)
    }

    private fun observeSnipsUpdate() {
        observe(presenter.snipsService.plateFormState) { getPlateformStateObserver() }
        observe(presenter.snipsService.snipsPlatformClientStatus) { getSnipsStatusObserver() }
    }

    private fun getSnipsStatusObserver(): Observer<String> {
        return Observer {
            // We enabled html logs in the builder, hence the fromHtml. If you only log to the console,
            // or don't want colors to be displayed, do not enable the option
            text.append(Html.fromHtml("$it<br />"))
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun getPlateformStateObserver(): Observer<PlateformState> {
        return Observer {
            when (it) {
                PlateformState.READY -> {
                    setupServiceWorkUI()
                    setupServiceReadyUI()
                }
                PlateformState.ERROR -> {
                    setupServiceWorkUI()
                }

                PlateformState.NOT_READY, PlateformState.LOADING -> {
                    setupServiceLoadingUI()
                }
            }
        }
    }

    private fun setupServiceLoadingUI() {
        loadingPanel.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        start.isEnabled = false
        stop.isEnabled = false
    }

    private fun setupServiceReadyUI() {
        loadingPanel.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
        start.isEnabled = true
        start.setText(R.string.start_dialog_session)
        start.setOnClickListener {
            // programmatically start a dialogue session
            presenter.startSnipSession()
        }
        start.setOnLongClickListener {
            // inject new values in the "house_room" entity
            presenter.snipsInjection()
            true
        }
    }

    private fun setupServiceWorkUI() {
        stop.isEnabled = true
        stop.text = getString(R.string.start)
        stop.setOnClickListener {
            SnipsService.stop(this)
            stop.text = getString(R.string.start)
            stop.setOnClickListener {
                SnipsService.start(this, PlateformState.NOT_READY.ordinal)
            }
        }
    }

    private fun setupUI() {
        if (ensurePermissions(this)) {
            start.setOnClickListener {
            }
            start.setOnLongClickListener {
                true
            }
        }
    }

    fun unBound() {
        if (presenter.unbound()) {
            unbindService(presenter)
        }
    }
}
