package ai.snips.snipsdemo

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
        presenter.plateFormState.observe(this, getPlateformStateObserver())
        presenter.snipsPlatformClientStatus.observe(this, getSnipsStatusObserver())
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
                PlateformState.ERROR -> {
                    loadingPanel.visibility = View.VISIBLE
                    scrollView.visibility = View.GONE
                    start.isEnabled = false
                }
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
