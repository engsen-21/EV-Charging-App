package smartcharge.master

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import smartcharge.master.data.Report
import smartcharge.master.databinding.ItemReportBinding

// Adapter class for the RecyclerView to display reports
class AdminReportsAdapter (private val reportsList: List<Report>) : RecyclerView.Adapter<AdminReportsAdapter.ReportViewHolder>(){
    // Method to create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        // Inflate the item layout for each report
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    // Method to replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        // Bind the report data to the view holder
        holder.bind(reportsList[position])
    }

    // Method to return the size of the dataset (invoked by the layout manager)
    override fun getItemCount(): Int = reportsList.size

    // ViewHolder class that provides a reference to each view in the item layout
    class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {

        // Method to bind the report data to the views
        fun bind(report: Report) {
            // Set the text views with report data
            binding.addressTextView.text = report.addressName
            binding.commentTextView.text = report.comment
            binding.usageTextView.text = report.usage
            binding.chargerTypeTextView.text = report.chargerType
            binding.userIdTextView.text = report.userId

            // Load the photo using Glide
            Glide.with(binding.root.context)
                // URL of the photo
                .load(report.photoUrl)
                .placeholder(R.drawable.ic_smart_charge) // Optional placeholder image
                .into(binding.photoUrl) // ImageView to load the photo into
        }
    }
}