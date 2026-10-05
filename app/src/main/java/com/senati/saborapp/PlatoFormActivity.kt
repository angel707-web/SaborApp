package com.senati.saborapp

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.senati.saborapp.dao.PlatoDao
import com.senati.saborapp.databinding.ActivityPlatoFormBinding
import com.senati.saborapp.model.Plato

class PlatoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlatoFormBinding
    private lateinit var platoDao: PlatoDao
    private var platoId: Int = -1
    private var esModoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        platoDao = PlatoDao(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar Spinner de categorías
        val adapterCategorias = ArrayAdapter.createFromResource(
            this,
            R.array.categorias_platos,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spCategoria.adapter = adapterCategorias

        // HU-07 (CA1): Detectar modo edición
        platoId = intent.getIntExtra("EXTRA_ID_PLATO", -1)
        if (platoId > 0) {
            esModoEdicion = true
            cargarDatosPlato()
        }

        // Guardar o Actualizar
        binding.btnGuardarPlato.setOnClickListener {
            guardarOActualizarPlato()
        }

        // HU-07 (CA2): Eliminar con diálogo y validación de pedidos asociados
        binding.btnEliminarPlato.setOnClickListener {
            confirmarEliminarPlato()
        }
    }

    private fun cargarDatosPlato() {
        val plato = platoDao.obtener(platoId) ?: return

        binding.toolbar.title = getString(R.string.titulo_editar_plato)
        binding.etNombre.setText(plato.nombre)
        binding.etPrecio.setText(plato.precio.toString())
        binding.swDisponible.isChecked = plato.disponible

        // Seleccionar categoría en el Spinner
        val categorias = resources.getStringArray(R.array.categorias_platos)
        val index = categorias.indexOf(plato.categoria)
        if (index >= 0) {
            binding.spCategoria.setSelection(index)
        }

        // CA1: Cambiar texto a «Actualizar»
        binding.btnGuardarPlato.text = getString(R.string.btn_actualizar)
        binding.btnEliminarPlato.visibility = View.VISIBLE
    }

    private fun guardarOActualizarPlato() {
        val nombre = binding.etNombre.text.toString().trim()
        val precioStr = binding.etPrecio.text.toString().trim()
        val categoria = binding.spCategoria.selectedItem?.toString() ?: "Fondos"
        val disponible = binding.swDisponible.isChecked

        binding.tilNombre.error = null
        binding.tilPrecio.error = null

        var valido = true

        if (nombre.isEmpty()) {
            binding.tilNombre.error = getString(R.string.err_nombre_requerido)
            valido = false
        }

        if (precioStr.isEmpty()) {
            binding.tilPrecio.error = getString(R.string.err_precio_requerido)
            valido = false
        } else {
            val precio = precioStr.toDoubleOrNull()
            if (precio == null || precio <= 0.0) {
                binding.tilPrecio.error = getString(R.string.err_precio_invalido)
                valido = false
            }
        }

        if (!valido) return

        val precioFinal = precioStr.toDouble()

        if (esModoEdicion) {
            val platoActualizado = Plato(
                id = platoId,
                nombre = nombre,
                categoria = categoria,
                precio = precioFinal,
                disponible = disponible
            )
            val filas = platoDao.actualizar(platoActualizado)
            if (filas > 0) {
                Toast.makeText(this, getString(R.string.msg_plato_actualizado), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        } else {
            val nuevoPlato = Plato(
                nombre = nombre,
                categoria = categoria,
                precio = precioFinal,
                disponible = disponible
            )
            val id = platoDao.insertar(nuevoPlato)
            if (id > 0) {
                Toast.makeText(this, getString(R.string.msg_plato_guardado), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * CA2: Diálogo de confirmación y captura de SQLiteConstraintException
     */
    private fun confirmarEliminarPlato() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_titulo_eliminar_plato)
            .setMessage(R.string.dialog_mensaje_eliminar_plato)
            .setPositiveButton(R.string.btn_confirmar) { _, _ ->
                ejecutarEliminarPlato()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun ejecutarEliminarPlato() {
        try {
            val filas = platoDao.eliminar(platoId)
            if (filas > 0) {
                Toast.makeText(this, getString(R.string.msg_plato_eliminado), Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: SQLiteConstraintException) {
            // CA2: Tiene pedidos asociados en detalle_pedido
            Toast.makeText(this, getString(R.string.err_plato_con_pedidos), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.err_plato_con_pedidos), Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
